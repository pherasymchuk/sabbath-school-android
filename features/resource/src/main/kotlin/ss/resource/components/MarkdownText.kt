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

package ss.resource.components

import android.widget.TextView
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.ss.design.compose.extensions.color.toAndroidColor
import app.ss.design.compose.theme.SsTheme
import io.noties.markwon.Markwon
import com.cryart.design.R as DesignR

/**
 * Composable that renders Markdown text using Markwon.
 *
 * @param text The markdown text to render.
 * @param modifier Modifier for this composable.
 */
@Composable
internal fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val markwon = remember { Markwon.create(context) }
    val contentColor = SsTheme.colors.primaryForeground

    AndroidView(
        factory = { ctx ->
            TextView(ctx).apply {
                markwon.setMarkdown(this, text)
                setTextColor(contentColor.toAndroidColor())
                setTextAppearance(DesignR.style.TextAppearance_Markdown)
            }
        },
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
private fun MarkdownTextPreview() {
    SsTheme {
        Surface {
            MarkdownText(
                text = """
                    # Sample Heading
                    
                    This is a **bold** text and *italic* text.
                    
                    - List item 1
                    - List item 2
                    
                    [Link](https://example.com)
                """.trimIndent(),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
