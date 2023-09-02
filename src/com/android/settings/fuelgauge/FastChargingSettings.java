/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.fuelgauge;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.alpha.settings.preferences.CustomSeekBarPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.lang.Integer;
import java.util.NoSuchElementException;

import vendor.lineage.fastcharge.V1_0.IFastCharge;

@SearchIndexable
public class FastChargingSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "FastChargingSettings";

    private static final String KEY_FAST_CHARGING = "fast_charging_switch";
    private static final String KEY_RESTRICTED_CURRENT = "restricted_current";

    private SwitchPreference mFastChargingSwitch;
    private CustomSeekBarPreference mRestrictedCurrentSeekBar;

    private IFastCharge mFastCharge = null;
    private int mMinCurrent = 0;
    private int mMaxCurrent = 0;
    private int mRestrictedCurrent = 0;
    private int mDefaultCurrent = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fast_charging_settings);

        final PreferenceScreen prefScreen = getPreferenceScreen();

        mFastChargingSwitch = findPreference(KEY_FAST_CHARGING);
        mRestrictedCurrentSeekBar = findPreference(KEY_RESTRICTED_CURRENT);

        try {
            mFastCharge = IFastCharge.getService();
        } catch (NoSuchElementException | RemoteException e) {
            Log.e(TAG, "Failed to get IFastCharge interface", e);
            prefScreen.removePreference(mFastChargingSwitch);
            prefScreen.removePreference(mRestrictedCurrentSeekBar);
            return;
        }

        boolean enable = isEnabled();;
        mFastChargingSwitch.setChecked(enable);
        mFastChargingSwitch.setOnPreferenceChangeListener(this);

        mMaxCurrent = getMaxCurrent();
        mMinCurrent = getMinCurrent();
        mDefaultCurrent = (mMaxCurrent + mMinCurrent) / 2;

        mRestrictedCurrentSeekBar.setMin(mMinCurrent);
        mRestrictedCurrentSeekBar.setMax(mMaxCurrent);
        mRestrictedCurrentSeekBar.setDefaultValue(mDefaultCurrent);

        mRestrictedCurrent = getRestrictedCurrent();
        mRestrictedCurrentSeekBar.setValue(mRestrictedCurrent);

        mRestrictedCurrentSeekBar.setEnabled(!enable);
        mRestrictedCurrentSeekBar.setOnPreferenceChangeListener(this);
    }

    private boolean isEnabled() {
        boolean fastChargingEnabled = false;
        try {
            fastChargingEnabled = mFastCharge.isEnabled();
        } catch (RemoteException e) {
            Log.e(TAG, "isEnabled failed", e);
        }
        return fastChargingEnabled;
    }

    private boolean setEnabled(boolean enable) {
        try {
            mFastCharge.setEnabled(enable);
        } catch (RemoteException e) {
            Log.e(TAG, "setEnabled failed", e);
            return false;
        }
        return enable;
    }

    private int getRestrictedCurrent() {
         int restrictedCurrent = 0;

        try {
            restrictedCurrent = mFastCharge.getRestrictedCurrent();
        } catch (RemoteException e) {
            Log.e(TAG, "getRestrictedCurrent failed", e);
            return 0;
        }

        if (restrictedCurrent < mMinCurrent) {
            restrictedCurrent = mMinCurrent;
        } else if (restrictedCurrent > mMaxCurrent) {
            restrictedCurrent = mMaxCurrent;
        }

        return restrictedCurrent;
    }

    private boolean setRestrictedCurrent(int current) {
        boolean success = false;
        try {
            success = mFastCharge.setRestrictedCurrent(current);
        } catch (RemoteException e) {
            Log.e(TAG, "setRestrictedCurrent failed", e);
        }
        return success;
    }

    private int getMaxCurrent() {
        try {
            mMaxCurrent = mFastCharge.getMaxCurrent();
        } catch (RemoteException e) {
            Log.e(TAG, "getMaxCurrent failed", e);
            return 0;
        }
        return mMaxCurrent;
    }

    private int getMinCurrent() {
        try {
            mMinCurrent = mFastCharge.getMinCurrent();
        } catch (RemoteException e) {
            Log.e(TAG, "getMinCurrent failed", e);
            return 0;
        }
        return mMinCurrent;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mFastChargingSwitch) {
            boolean enable = setEnabled((Boolean) newValue);
            mFastChargingSwitch.setChecked(enable);
            mRestrictedCurrentSeekBar.setEnabled(!enable);
            return true;
        } else if (preference == mRestrictedCurrentSeekBar) {
            int current = (Integer) newValue;
            boolean success = setRestrictedCurrent(current);
            if (success) {
                mRestrictedCurrentSeekBar.setValue(current);
                mRestrictedCurrent = current;
            }
            else {
                /* restore previous value */
                mRestrictedCurrentSeekBar.setValue(mRestrictedCurrent);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALPHA;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.fast_charging_settings);

}
