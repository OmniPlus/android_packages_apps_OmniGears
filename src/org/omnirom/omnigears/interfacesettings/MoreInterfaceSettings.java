/*
 *  Copyright (C) 2013 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.omnirom.omnigears.interfacesettings;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;

import org.omnirom.omnigears.chameleonos.AppMultiSelectListPreference;
import com.android.internal.util.omni.DeviceUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MoreInterfaceSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "MoreInterfaceSettings";

    private static final String RECENT_MENU_CLEAR_ALL = "recent_menu_clear_all";
    private static final String RECENT_MENU_CLEAR_ALL_LOCATION = "recent_menu_clear_all_location";
    private static final String SHOW_RECENTS_MEMORY_INDICATOR = "show_recents_memory_indicator";
    private static final String RECENTS_MEMORY_INDICATOR_LOCATION =
            "recents_memory_indicator_location";
    private static final String RECENTS_USE_OMNISWITCH = "recents_use_omniswitch";
    private static final String OMNISWITCH_START_SETTINGS = "omniswitch_start_settings";
    private static final String CATEGORY_USE_OMNISWITCH = "category_omniswitch";

    // Package name of the omnniswitch app
    public static final String OMNISWITCH_PACKAGE_NAME = "org.omnirom.omniswitch";
    // Intent for launching the omniswitch settings actvity
    public static Intent INTENT_OMNISWITCH_SETTINGS = new Intent(Intent.ACTION_MAIN)
            .setClassName(OMNISWITCH_PACKAGE_NAME, OMNISWITCH_PACKAGE_NAME + ".SettingsActivity");

    private static final String PREF_INCLUDE_APP_CIRCLE_BAR_KEY = "app_circle_bar_included_apps";

    private CheckBoxPreference mRecentClearAll;
    private ListPreference mRecentClearAllPosition;
    private CheckBoxPreference mShowRecentsMemoryIndicator;
    private ListPreference mRecentsMemoryIndicatorPosition;
    private CheckBoxPreference mRecentsUseOmniSwitch;
    private Preference mOmniSwitchSettings;
    private boolean mOmniSwitchInitCalled;
    private PreferenceCategory mOmniSwitchCategory;

    private AppMultiSelectListPreference mIncludedAppCircleBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.more_interface_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mIncludedAppCircleBar = (AppMultiSelectListPreference) prefSet.findPreference(PREF_INCLUDE_APP_CIRCLE_BAR_KEY);
        Set<String> includedApps = getIncludedApps();
        if (includedApps != null) mIncludedAppCircleBar.setValues(includedApps);
        mIncludedAppCircleBar.setOnPreferenceChangeListener(this);

        mRecentClearAll = (CheckBoxPreference) prefSet.findPreference(RECENT_MENU_CLEAR_ALL);
        mRecentClearAll.setChecked(Settings.System.getInt(resolver,
                Settings.System.SHOW_CLEAR_RECENTS_BUTTON, 0) == 1);
        mRecentClearAll.setOnPreferenceChangeListener(this);
        mRecentClearAllPosition = (ListPreference) prefSet.findPreference(RECENT_MENU_CLEAR_ALL_LOCATION);
        String recentClearAllPosition = Settings.System.getString(resolver, Settings.System.CLEAR_RECENTS_BUTTON_LOCATION);
        if (recentClearAllPosition != null) {
            mRecentClearAllPosition.setValue(recentClearAllPosition);
        }
        mRecentClearAllPosition.setOnPreferenceChangeListener(this);

        mShowRecentsMemoryIndicator = (CheckBoxPreference)
                prefSet.findPreference(SHOW_RECENTS_MEMORY_INDICATOR);
        mShowRecentsMemoryIndicator.setChecked(Settings.System.getInt(resolver,
                Settings.System.SHOW_RECENTS_MEMORY_INDICATOR, 0) == 1);
        mShowRecentsMemoryIndicator.setOnPreferenceChangeListener(this);
        mRecentsMemoryIndicatorPosition = (ListPreference) prefSet
                .findPreference(RECENTS_MEMORY_INDICATOR_LOCATION);
        String recentsMemoryIndicatorPosition = Settings.System.getString(
                resolver, Settings.System.RECENTS_MEMORY_INDICATOR_LOCATION);
        if (recentsMemoryIndicatorPosition != null) {
            mRecentsMemoryIndicatorPosition.setValue(recentsMemoryIndicatorPosition);
        }
        mRecentsMemoryIndicatorPosition.setOnPreferenceChangeListener(this);

        mOmniSwitchCategory = (PreferenceCategory) prefSet.findPreference(CATEGORY_USE_OMNISWITCH);
        boolean omniSwitchAvailable = isOmniSwitchAvailable();

        if (!omniSwitchAvailable){
            Settings.System.putInt(resolver, Settings.System.RECENTS_USE_OMNISWITCH, 0);
            prefSet.removePreference(mOmniSwitchCategory);
        } else {
            mRecentsUseOmniSwitch = (CheckBoxPreference)
                    prefSet.findPreference(RECENTS_USE_OMNISWITCH);
            mRecentsUseOmniSwitch.setChecked(Settings.System.getInt(resolver,
                    Settings.System.RECENTS_USE_OMNISWITCH, 0) == 1);
            mRecentsUseOmniSwitch.setOnPreferenceChangeListener(this);

            mOmniSwitchSettings = (Preference)
                    prefSet.findPreference(OMNISWITCH_START_SETTINGS);
            mOmniSwitchSettings.setEnabled(mRecentsUseOmniSwitch.isChecked());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mOmniSwitchSettings){
            startActivity(INTENT_OMNISWITCH_SETTINGS);
            return true;
        }
        // If we didn't handle it, let preferences handle it.
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mRecentClearAll) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver, Settings.System.SHOW_CLEAR_RECENTS_BUTTON, value ? 1 : 0);
        } else if (preference == mRecentClearAllPosition) {
            String value = (String) objValue;
            Settings.System.putString(resolver, Settings.System.CLEAR_RECENTS_BUTTON_LOCATION, value);
        } else if (preference == mShowRecentsMemoryIndicator) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(
                    resolver, Settings.System.SHOW_RECENTS_MEMORY_INDICATOR, value ? 1 : 0);
        } else if (preference == mRecentsMemoryIndicatorPosition) {
            String value = (String) objValue;
            Settings.System.putString(
                    resolver, Settings.System.RECENTS_MEMORY_INDICATOR_LOCATION, value);
        } else if (preference == mIncludedAppCircleBar) {
            storeIncludedApps((Set<String>) objValue);
        } else if (preference == mRecentsUseOmniSwitch) {
            boolean value = (Boolean) objValue;

            // if value has never been set before
            if (value && !mOmniSwitchInitCalled){
                openOmniSwitchFirstTimeWarning();
                mOmniSwitchInitCalled = true;
            }

            Settings.System.putInt(
                    resolver, Settings.System.RECENTS_USE_OMNISWITCH, value ? 1 : 0);
            mOmniSwitchSettings.setEnabled(value);
        } else {
            return false;
        }

        return true;
    }

    private void openOmniSwitchFirstTimeWarning() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.omniswitch_first_time_title))
                .setMessage(getResources().getString(R.string.omniswitch_first_time_message))
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                }).show();
    }

    private boolean isOmniSwitchAvailable() {
        final PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(OMNISWITCH_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            int enabled = pm.getApplicationEnabledSetting(OMNISWITCH_PACKAGE_NAME);
            return enabled != PackageManager.COMPONENT_ENABLED_STATE_DISABLED &&
                enabled != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private Set<String> getIncludedApps() {
        String included = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.WHITELIST_APP_CIRCLE_BAR);
        if (TextUtils.isEmpty(included)) {
            return null;
        }
        return new HashSet<String>(Arrays.asList(included.split("\\|")));
    }

    private void storeIncludedApps(Set<String> values) {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";
        for (String value : values) {
            builder.append(delimiter);
            builder.append(value);
            delimiter = "|";
        }
        Settings.System.putString(getActivity().getContentResolver(),
                Settings.System.WHITELIST_APP_CIRCLE_BAR, builder.toString());
    }
}
