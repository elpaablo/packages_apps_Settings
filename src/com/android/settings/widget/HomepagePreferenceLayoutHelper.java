/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.widget;

import static com.android.settings.alpha.AlphaConstants.DASHBOARD_STYLE_AOSP_LEGACY;
import static com.android.settings.alpha.AlphaConstants.DASHBOARD_STYLE_AOSP_REVAMPED;
import static com.android.settings.alpha.AlphaConstants.DASHBOARD_STYLE_DOT;
import static com.android.settings.alpha.AlphaConstants.DASHBOARD_STYLE_NAD;

import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.Utils;


/** Helper for homepage preference to manage layout. */
public class HomepagePreferenceLayoutHelper {

    private static final String NAD_TOP_MENU_KEY = "nad_top_menu_key";

    private View mIcon;
    private View mChevron;
    private View mText;
    private boolean mIconVisible = true;
    private int mIconPaddingStart = -1;
    private int mTextPaddingStart = -1;

    /** The interface for managing preference layouts on homepage */
    public interface HomepagePreferenceLayout {
        /** Returns a {@link HomepagePreferenceLayoutHelper}  */
        HomepagePreferenceLayoutHelper getHelper();
    }

    public HomepagePreferenceLayoutHelper(Preference preference) {
        int dashBoardStyle = Utils.getDashboardStyle(preference.getContext());

        if (preference == null) return;

        // DOT style is handled dynamically by TopLevelSettings
        switch (dashBoardStyle) {
            case DASHBOARD_STYLE_AOSP_LEGACY:
                preference.setLayoutResource(R.layout.homepage_preference);
                break;
            case DASHBOARD_STYLE_AOSP_REVAMPED:
                preference.setLayoutResource(R.layout.homepage_preference_v2);
                break;
            case DASHBOARD_STYLE_NAD:
                if (!NAD_TOP_MENU_KEY.equals(preference.getKey())) {
                    preference.setLayoutResource(R.layout.nad_homepage_preference);
                }
                break;
            default:
                break;
        }
    }

    /** Sets whether the icon should be visible */
    public void setIconVisible(boolean visible) {
        mIconVisible = visible;
        if (mIcon != null) {
            mIcon.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /** Sets the icon padding start */
    public void setIconPaddingStart(int paddingStart) {
        mIconPaddingStart = paddingStart;
        if (mIcon != null && paddingStart >= 0) {
            mIcon.setPaddingRelative(paddingStart, mIcon.getPaddingTop(), mIcon.getPaddingEnd(),
                    mIcon.getPaddingBottom());
        }
    }

    /** Sets the text padding start */
    public void setTextPaddingStart(int paddingStart) {
        mTextPaddingStart = paddingStart;
        if (mText != null && paddingStart >= 0) {
            mText.setPaddingRelative(paddingStart, mText.getPaddingTop(), mText.getPaddingEnd(),
                    mText.getPaddingBottom());
        }
    }

    void onBindViewHolder(PreferenceViewHolder holder) {
        mIcon = holder.findViewById(R.id.icon_frame);
        mText = holder.findViewById(R.id.text_frame);
        setIconVisible(mIconVisible);
        setIconPaddingStart(mIconPaddingStart);
        setTextPaddingStart(mTextPaddingStart);
    }
}
