/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.navigation

import android.content.Context
import android.content.Intent
import android.os.Build
import app.ss.widgets.AppWidgetAction
import com.cryart.sabbathschool.ui.home.HomeActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppWidgetActionImpl @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
) : AppWidgetAction {

    override fun launchLesson(quarterlyIndex: String): Intent =
        Intent(appContext, HomeActivity::class.java).apply {
            putExtra(AppNavigatorImpl.EXTRA_RESOURCE_INDEX, quarterlyIndex.toResourceIndex())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

    override fun launchRead(lessonIndex: String, dayIndex: String?): Intent =
        Intent(appContext, HomeActivity::class.java).apply {
            putExtra(AppNavigatorImpl.EXTRA_DOCUMENT_INDEX, lessonIndex.toDocumentIndex())
            dayIndex?.let { putExtra(EXTRA_SEGMENT_INDEX, it) }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // See [Intent.filterEquals].
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                identifier = "$lessonIndex/${dayIndex.orEmpty()}"
            }
        }

    companion object {
        const val EXTRA_SEGMENT_INDEX = "extra_segment_index"
    }
}
