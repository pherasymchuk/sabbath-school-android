/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ss.lessons.impl.helper

import app.ss.models.OfflineState
import app.ss.models.SSQuarterlyInfo
import app.ss.network.NetworkResource
import app.ss.network.safeApiCall
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import ss.foundation.android.connectivity.ConnectivityHelper
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.ioScopable
import ss.lessons.api.SSQuarterliesApi
import ss.lessons.api.helper.SyncHelper
import ss.lessons.impl.ext.toEntity
import ss.lessons.model.request.PublishingInfoRequest
import ss.libraries.storage.api.dao.LessonsDao
import ss.libraries.storage.api.dao.PublishingInfoDao
import ss.libraries.storage.api.dao.QuarterliesDao
import ss.libraries.storage.api.entity.PublishingInfoEntity
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SyncHelperImpl @Inject constructor(
    private val quarterliesApi: SSQuarterliesApi,
    private val quarterliesDao: QuarterliesDao,
    private val publishingInfoDao: PublishingInfoDao,
    private val lessonsDao: LessonsDao,
    private val connectivityHelper: ConnectivityHelper,
    dispatcherProvider: DispatcherProvider
) : SyncHelper, Scopable by ioScopable(dispatcherProvider) {

    private val exceptionLogger = CoroutineExceptionHandler { _, exception -> Timber.e(exception) }

    override suspend fun syncQuarterlyInfo(index: String): SSQuarterlyInfo? {
        val language = index.substringBefore('-')
        val id = index.substringAfter('-')

        return when (val response = safeApiCall(connectivityHelper) { quarterliesApi.getQuarterlyInfo(language, id) }) {
            is NetworkResource.Failure -> {
                Timber.e("Failed to fetch Quarterly: isNetwork=${response.isNetworkError}, ${response.errorBody}")
                null
            }

            is NetworkResource.Success -> response.value.body()?.let { quarterlyInfo ->
                saveQuarterlyInfo(quarterlyInfo)
                return quarterlyInfo
            }
        }
    }

    override fun syncQuarterlies(language: String) {
        scope.launch(exceptionLogger) {
            when (val response = safeApiCall(connectivityHelper) { quarterliesApi.getQuarterlies(language) }) {
                is NetworkResource.Failure -> Timber.e("Failed to fetch quarterlies: isNetwork=${response.isNetworkError}, ${response.errorBody}")
                is NetworkResource.Success -> response.value.body()?.let { quarterlies ->
                    val entities = quarterlies.map {
                        val state = quarterliesDao.getOfflineState(it.index) ?: OfflineState.NONE
                        it.toEntity(state)
                    }
                    quarterliesDao.insertAll(entities)
                }
            }
        }
    }

    private suspend fun saveQuarterlyInfo(info: SSQuarterlyInfo) {
        val quarterIndex = info.quarterly.index
        info.lessons.forEachIndexed { index, lesson ->
            lessonsDao.get(lesson.index)?.let { entity ->
                lessonsDao.update(
                    lesson.toEntity(
                        quarter = quarterIndex,
                        order = index,
                        days = entity.days,
                        pdfs = entity.pdfs,
                    )
                )
            } ?: run { lessonsDao.insertItem(lesson.toEntity(quarterIndex, order = index)) }
        }
        val state = quarterliesDao.getOfflineState(info.quarterly.index) ?: OfflineState.NONE
        quarterliesDao.update(info.quarterly.toEntity(state))
    }

    override fun syncPublishingInfo(country: String, language: String) {
        scope.launch(exceptionLogger) {
            when (val response = safeApiCall(connectivityHelper) {
                quarterliesApi.getPublishingInfo(PublishingInfoRequest(country, language))
            }) {
                is NetworkResource.Failure -> Unit
                is NetworkResource.Success -> response.value.body()?.data?.let {
                    publishingInfoDao.insertItem(
                        PublishingInfoEntity(
                            country = country,
                            language = language,
                            message = it.message,
                            url = it.url,
                        )
                    )
                }
            }
        }
    }
}
