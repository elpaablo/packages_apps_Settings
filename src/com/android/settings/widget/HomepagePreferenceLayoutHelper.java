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

import android.content.Context;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.Utils;

/** Helper for homepage preference to manage layout. */
public class HomepagePreferenceLayoutHelper {

    private View mIcon;
    private View mText;
    private View mChevron;
    private boolean mIconVisible = true;
    private boolean mChevronVisible = false;
    private int mIconPaddingStart = -1;
    private int mTextPaddingStart = -1;

    private static Boolean sRevamped = null;
    private int mDashboardStyle = -1;
    private static Context sContext;

    /** The interface for managing preference layouts on homepage */
    public interface HomepagePreferenceLayout {
        /** Returns a {@link HomepagePreferenceLayoutHelper}  */
        HomepagePreferenceLayoutHelper getHelper();
    }

    public HomepagePreferenceLayoutHelper(Preference preference) {
        Context context = preference.getContext();
        sRevamped = revamped(context);
        mDashboardStyle = getDashboardStyle(context);
        sContext = context.getApplicationContext();
        if (mDashboardStyle == 2) {
            mChevronVisible = true;
        }
        setLayoutResource(preference);
    }

    private void setLayoutResource(Preference preference) {
        int dashBoardStyle = getDashboardStyle();
        switch(dashBoardStyle) {
            case 0: // AOSP legacy
                preference.setLayoutResource(R.layout.homepage_preference);
                break;
            case 2: // DoT
                preference.setLayoutResource(R.layout.dot_homepage_preference);
                break;
            case 3: // NAD
                preference.setLayoutResource(R.layout.nad_homepage_preference);
                break;
            default: // AOSP revamped
                preference.setLayoutResource(R.layout.homepage_preference_v2);
        }
    }

    private static boolean revamped() {
        return revamped(sContext);
    }

    private static boolean revamped(Context context) {
        if (sRevamped == null) {
            sRevamped = Boolean.valueOf(Utils.revamped(context));
        }
        return sRevamped.booleanValue();
    }

    private int getDashboardStyle() {
        return getDashboardStyle(sContext);
    }

    private int getDashboardStyle(Context context) {
        if (mDashboardStyle == -1) {
            mDashboardStyle = Utils.getDashboardStyle(context);
        }
        return mDashboardStyle;
    }

    /** Sets whether the icon should be visible */
    public void setIconVisible(boolean visible) {
        mIconVisible = visible;
        if (mIcon != null) {
            mIcon.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /** Sets whether the chevron icon should be visible */
    public void setChevronVisible(boolean visible) {
        mChevronVisible = visible;
        if (mChevron != null) {
            mChevron.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /** Sets the icon paddings */
    public void setIconPaddingStart(int paddingStart) {
        mIconPaddingStart = paddingStart;
        if (paddingStart >= 0) {
            if (mIcon != null) {
                mIcon.setPaddingRelative(paddingStart, mIcon.getPaddingTop(), paddingStart,
                        mIcon.getPaddingBottom());
            }
            if (getDashboardStyle() == 2 && mChevron != null) {
                mChevron.setPaddingRelative(paddingStart, mChevron.getPaddingTop(), paddingStart,
                        mChevron.getPaddingBottom());
            }
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
        mChevron = holder.findViewById(R.id.chevron_frame);
        setIconVisible(mIconVisible);
        setChevronVisible(mChevronVisible);
        setIconPaddingStart(mIconPaddingStart);
        setTextPaddingStart(mTextPaddingStart);
    }
}
