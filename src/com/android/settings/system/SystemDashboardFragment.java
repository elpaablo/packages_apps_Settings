/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.settings.system;

import android.content.ContentResolver;
import android.content.Context;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.provider.Settings.Secure;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.custom.KernelProfileManager;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.utils.Util;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SearchIndexable
public class SystemDashboardFragment extends DashboardFragment implements
        Preference.OnPreferenceChangeListener{

    private static final String TAG = "SystemDashboardFrag";

    private static final String KEY_RESET = "reset_dashboard";

    public static final String EXTRA_SHOW_AWARE_DISABLED = "show_aware_dialog_disabled";
    
    public static final String KERNEL_PROFILES_ENABLED_KEY = "kernel_profiles_enabled";
    
    public static final String KERNEL_PROFILE = "kernel_profile";
    
    private static final String PREF_SYSTEM_APP_REMOVER = "system_app_remover";
    private static final String PREF_ADBLOCK = "persist.aicp.hosts_block";
    
    private SwitchPreference mKernelProfilesPreference;
    private KernelProfileManager manager;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final PreferenceScreen screen = getPreferenceScreen();
        // We do not want to display an advanced button if only one setting is hidden
        if (getVisiblePreferenceCount(screen) == screen.getInitialExpandedChildrenCount() + 1) {
            screen.setInitialExpandedChildrenCount(Integer.MAX_VALUE);
        }
        
        mKernelProfilesPreference = (SwitchPreference) findPreference(KERNEL_PROFILES_ENABLED_KEY);
        mKernelProfilesPreference.setChecked(Settings.Secure.getIntForUser(getContentResolver(),
                    Settings.Secure.KERNEL_PROFILES_ENABLED, 0, UserHandle.USER_CURRENT) == 1);
        mKernelProfilesPreference.setOnPreferenceChangeListener(this);
        
        Preference systemAppRemover = findPreference(PREF_SYSTEM_APP_REMOVER);
        Util.requireRoot(getActivity(), systemAppRemover);

        findPreference(PREF_ADBLOCK).setOnPreferenceChangeListener(this);
        
        showRestrictionDialog();
    }

    @VisibleForTesting
    public void showRestrictionDialog() {
        final Bundle args = getArguments();
        if (args != null && args.getBoolean(EXTRA_SHOW_AWARE_DISABLED, false)) {
            FeatureFactory.getFactory(getContext()).getAwareFeatureProvider()
                    .showRestrictionDialog(this);
        }
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_SYSTEM_CATEGORY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.system_dashboard_fragment;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_system_dashboard;
    }

    private int getVisiblePreferenceCount(PreferenceGroup group) {
        int visibleCount = 0;
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            final Preference preference = group.getPreference(i);
            if (preference instanceof PreferenceGroup) {
                visibleCount += getVisiblePreferenceCount((PreferenceGroup) preference);
            } else if (preference.isVisible()) {
                visibleCount++;
            }
        }
        return visibleCount;
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mKernelProfilesPreference) {
            boolean value = (Boolean) objValue;
            mKernelProfilesPreference.setChecked(value);
            Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.KERNEL_PROFILES_ENABLED, value ? 1 : 0);
            
            //if enabling kernel profiles, apply saved profile
            if (value){ 
                manager = new KernelProfileManager(getContext());
                int profile = Settings.Secure.getIntForUser(getContentResolver(),
                        Settings.Secure.KERNEL_PROFILE, KernelProfileManager.PROFILE_DEFAULT,
                        UserHandle.USER_CURRENT);
                manager.setProfile(profile);
            }
            return true;
        } else if (PREF_ADBLOCK.equals(preference.getKey())) {
            // Flush the java VM DNS cache to re-read the hosts file.
            // Delay to ensure the value is persisted before we refresh
            mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InetAddress.clearDnsCache();
                    }
            }, 1000);
            return true;
        }
       
        return false;
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.system_dashboard_fragment;
                    return Arrays.asList(sir);
                }
                
                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> result = new ArrayList<String>();
                    result.add(KERNEL_PROFILES_ENABLED_KEY);
                    return result;
                }
            };
}
