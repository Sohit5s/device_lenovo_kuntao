/*
* Copyright (C) 2016 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.com/licenses/>.
*
*/
package com.lineageos.settings.device;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.TwoStatePreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.util.Log;

public class DeviceSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String KEY_BATTERY_CHARGING_LIMITER = "battery_charging_limiter";
    public static final String KEY_VIBSTRENGTH = "vib_strength";
    public static final String KEY_YELLOW_TORCH_BRIGHTNESS = "yellow_torch_brightness";
    public static final String KEY_WHITE_TORCH_BRIGHTNESS = "white_torch_brightness";
    //public static final String KEY_GLOVE_MODE = "glove_mode";
    public static final String USB_FASTCHARGE_KEY = "fastcharge";
    public static final String USB_FASTCHARGE_PATH = "/sys/kernel/fast_charge/force_fast_charge";
    public static final String FILE_S2S_TYPE = "/sys/sweep2sleep/sweep2sleep";
    private static final String GLOVE_MODE_FILE = "/sys/board_properties/tpd_glove_status";

    private static final String SPECTRUM_KEY = "spectrum";
    private static final String SPECTRUM_SYSTEM_PROPERTY = "persist.spectrum.profile";
    public static final String S2S_KEY = "sweep2sleep";
    private static final String KEY_CATEGORY_USB_FASTCHARGE = "usb_fastcharge";

    private VibratorStrengthPreference mVibratorStrength;
    private YellowTorchBrightnessPreference mYellowTorchBrightness;
    private WhiteTorchBrightnessPreference mWhiteTorchBrightness;
    //private TwoStatePreference mGloveMode;
    private ListPreference mSpectrum;
    private SwitchPreference mFastcharge;
    private PreferenceCategory mUsbFastcharge;
    private BatteryChargingLimiterPreference mBatteryChargingLimiter;
	private ListPreference mS2S;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main, rootKey);

        PreferenceScreen prefSet = getPreferenceScreen();

        PreferenceScreen mKcalPref = (PreferenceScreen) findPreference("kcal");
        mKcalPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
             @Override
             public boolean onPreferenceClick(Preference preference) {
                 Intent intent = new Intent(getActivity().getApplicationContext(), DisplayCalibration.class);
                 startActivity(intent);
                 return true;
             }
        });

        mVibratorStrength = (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        if (mVibratorStrength != null) {
            mVibratorStrength.setEnabled(VibratorStrengthPreference.isSupported());
        }

        mYellowTorchBrightness = (YellowTorchBrightnessPreference) findPreference(KEY_YELLOW_TORCH_BRIGHTNESS);
        if (mYellowTorchBrightness != null) {
            mYellowTorchBrightness.setEnabled(YellowTorchBrightnessPreference.isSupported());
        }

        mWhiteTorchBrightness = (WhiteTorchBrightnessPreference) findPreference(KEY_WHITE_TORCH_BRIGHTNESS);
        if (mWhiteTorchBrightness != null) {
            mWhiteTorchBrightness.setEnabled(WhiteTorchBrightnessPreference.isSupported());
        }

        /*mGloveMode = (TwoStatePreference) findPreference(KEY_GLOVE_MODE);
        mGloveMode.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(DeviceSettings.KEY_GLOVE_MODE, false));
        mGloveMode.setOnPreferenceChangeListener(this);*/

		mS2S = (ListPreference) findPreference(S2S_KEY);
        mS2S.setValue(Utils.getFileValue(FILE_S2S_TYPE, "0"));
		mS2S.setOnPreferenceChangeListener(this);

        mSpectrum = (ListPreference) findPreference(SPECTRUM_KEY);
        if( mSpectrum != null ) {
            mSpectrum.setValue(SystemProperties.get(SPECTRUM_SYSTEM_PROPERTY, "0"));
            mSpectrum.setOnPreferenceChangeListener(this);
            mSpectrum.setSummary(mSpectrum.getEntry());
        }

        mFastcharge = (SwitchPreference) findPreference(USB_FASTCHARGE_KEY);
        mFastcharge.setChecked(Utils.getFileValueAsBoolean(USB_FASTCHARGE_PATH, false));
        mFastcharge.setOnPreferenceChangeListener(this);

        mBatteryChargingLimiter = (BatteryChargingLimiterPreference) findPreference(KEY_BATTERY_CHARGING_LIMITER);
        if (mBatteryChargingLimiter != null) {
            mBatteryChargingLimiter.setEnabled(BatteryChargingLimiterPreference.isSupported());
        }
    }

	public static void setFastcharge(boolean value) {
        if (value) 
            Utils.writeValue(USB_FASTCHARGE_PATH, "1");
        else
            Utils.writeValue(USB_FASTCHARGE_PATH, "0"); 
    }

    /*public static void restore(Context context) {
        boolean gloveModeData = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DeviceSettings.KEY_GLOVE_MODE, false);
        Utils.writeValue(GLOVE_MODE_FILE, gloveModeData ? "1" : "0");
    }*/

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mS2S) {
            String strvalue = (String) newValue;
            Utils.writeValue(FILE_S2S_TYPE, strvalue);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            editor.putString(S2S_KEY, strvalue);
            editor.apply();
            return true;
		} /*else if (preference == mGloveMode) {
            Boolean enabled = (Boolean) newValue;
            SharedPreferences.Editor prefChange = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            prefChange.putBoolean(KEY_GLOVE_MODE, enabled).commit();
            Utils.writeValue(GLOVE_MODE_FILE, enabled ? "1" : "0");
            return true;
        }*/ else if (preference == mSpectrum) {
            String strvalue = (String) newValue;
            int index = mSpectrum.findIndexOfValue(strvalue);
            mSpectrum.setSummary(mSpectrum.getEntries()[index]);
            SystemProperties.set(SPECTRUM_SYSTEM_PROPERTY, strvalue);
            return true;
        } else if (preference == mFastcharge) {
            boolean value = (Boolean) newValue;
            mFastcharge.setChecked(value);
            setFastcharge(value);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            editor.putBoolean(USB_FASTCHARGE_KEY, value);
            editor.apply();
            return true;
        }
        return false;
    }

    public static void restoreSpectrumProp(Context context) {
        String spectrumStoredValue = PreferenceManager.getDefaultSharedPreferences(context).getString(SPECTRUM_KEY, "0");
        SystemProperties.set(SPECTRUM_SYSTEM_PROPERTY, spectrumStoredValue);
    }
}
