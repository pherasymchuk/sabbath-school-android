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

package app.ss.media.di

import app.ss.media.playback.ui.nowPlaying.AudioPlayerScreen
import app.ss.media.playback.ui.nowPlaying.mini.MiniPlayerScreen
import app.ss.media.playback.ui.video.VideosScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ss.libraries.navigation3.AudioPlayerKey
import ss.libraries.navigation3.EntryProviderBuilder
import ss.libraries.navigation3.ExpandedAudioPlayerKey
import ss.libraries.navigation3.MiniAudioPlayerKey
import ss.libraries.navigation3.VideosKey

@Module
@InstallIn(SingletonComponent::class)
object MediaNavModule {

    @Provides
    @IntoSet
    fun provideAudioPlayerEntry(): EntryProviderBuilder = {
        entry<AudioPlayerKey> { key ->
            AudioPlayerScreen(
                resourceId = key.resourceId,
                segmentId = key.segmentId,
            )
        }
    }

    @Provides
    @IntoSet
    fun provideExpandedAudioPlayerEntry(): EntryProviderBuilder = {
        entry<ExpandedAudioPlayerKey> {
            AudioPlayerScreen(
                resourceId = "",
                segmentId = null,
            )
        }
    }

    @Provides
    @IntoSet
    fun provideMiniPlayerEntry(): EntryProviderBuilder = {
        entry<MiniAudioPlayerKey> {
            MiniPlayerScreen()
        }
    }

    @Provides
    @IntoSet
    fun provideVideosEntry(): EntryProviderBuilder = {
        entry<VideosKey> { key ->
            VideosScreen(
                documentIndex = key.documentIndex,
                documentId = key.documentId,
            )
        }
    }
}
