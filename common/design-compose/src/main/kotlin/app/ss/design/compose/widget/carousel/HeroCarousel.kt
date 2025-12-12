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

package app.ss.design.compose.widget.carousel

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Hero Carousel for displaying featured content.
 *
 * This component provides a horizontally scrolling carousel with a centered "hero" item
 * that scales up for emphasis, while adjacent items are smaller. It uses physics-based
 * spring animations from the Expressive motion scheme.
 *
 * @param itemCount The total number of items in the carousel.
 * @param modifier Modifier to be applied to the carousel.
 * @param preferredItemWidth The preferred width of each item in the carousel.
 * @param itemSpacing Spacing between items.
 * @param contentPadding Padding around the carousel content.
 * @param content The composable content for each item, receiving the item index.
 */
@Composable
fun <T> HeroCarousel(
    items: List<T>,
    modifier: Modifier = Modifier,
    preferredItemWidth: Dp = 200.dp,
    itemSpacing: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    key: ((T) -> Any)? = null,
    itemContent: @Composable (item: T) -> Unit,
) {
    if (items.isEmpty()) return

    val carouselState = rememberCarouselState { items.size }

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        preferredItemWidth = preferredItemWidth,
        modifier = modifier,
        itemSpacing = itemSpacing,
        contentPadding = contentPadding,
        flingBehavior = CarouselDefaults.singleAdvanceFlingBehavior(carouselState),
    ) { index ->
        val item = items[index]
        itemContent(item)
    }
}

/**
 * Simplified Hero Carousel that takes an item count and index-based content.
 */
@Composable
fun HeroCarouselIndexed(
    itemCount: Int,
    modifier: Modifier = Modifier,
    preferredItemWidth: Dp = 200.dp,
    itemSpacing: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    itemContent: @Composable (index: Int) -> Unit,
) {
    if (itemCount == 0) return

    val carouselState = rememberCarouselState { itemCount }

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        preferredItemWidth = preferredItemWidth,
        modifier = modifier,
        itemSpacing = itemSpacing,
        contentPadding = contentPadding,
        flingBehavior = CarouselDefaults.singleAdvanceFlingBehavior(carouselState),
    ) { index ->
        itemContent(index)
    }
}
