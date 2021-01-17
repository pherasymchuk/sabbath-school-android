/*
 * Copyright (c) 2020 Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cryart.sabbathschool.lessons.ui.viewmodel;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import androidx.databinding.BaseObservable;
import androidx.databinding.BindingAdapter;

import com.cryart.sabbathschool.lessons.data.model.SSQuarterly;
import com.cryart.sabbathschool.lessons.ui.util.ImageUtil;
import com.google.android.material.button.MaterialButton;

public class SSQuarterlyItemViewModel extends BaseObservable implements SSViewModel {
    private SSQuarterly ssQuarterly;

    public SSQuarterlyItemViewModel(SSQuarterly ssQuarterly) {
        this.ssQuarterly = ssQuarterly;
    }

    public void setSsQuarterly(SSQuarterly ssQuarterly) {
        this.ssQuarterly = ssQuarterly;
        notifyChange();
    }

    public String getTitle() {
        return ssQuarterly.title;
    }

    public String getDate() {
        return ssQuarterly.human_date;
    }

    public String getCover() {
        return ssQuarterly.cover;
    }

    public String getDescription() {
        return ssQuarterly.description;
    }

    public Integer getColorPrimary() {
        return Color.parseColor(ssQuarterly.color_primary);
    }

    public String getColorPrimaryDark() {
        return ssQuarterly.color_primary_dark;
    }

    @BindingAdapter({"backgroundColor"})
    public static void setBackgroundColor(View view, String color) {
        view.setBackgroundColor(Color.parseColor(color));
    }

    @BindingAdapter({"fbDefaultColor"})
    public static void setFbDefaultColor(MaterialButton view, String color) {
        view.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
    }

    @BindingAdapter({"coverUrl"})
    public static void loadCover(ImageView view, String coverUrl) {
        ImageUtil.load(view, coverUrl);
    }

    public void onReadClick(View view) {
        //TODO: Start LessonsActivity
        /*Intent ssLessonsIntent = new Intent(context, SSLessonsActivity.class);
        ssLessonsIntent.putExtra(SSConstants.SS_QUARTERLY_INDEX_EXTRA, ssQuarterly.index);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation((AppCompatActivity) context, view, context.getString(R.string.ss_quarterly_cover_transition));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.startActivity(ssLessonsIntent, options.toBundle());
        } else {
            context.startActivity(ssLessonsIntent);
        }*/
    }

    @Override
    public void destroy() {

    }
}