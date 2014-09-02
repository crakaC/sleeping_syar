package com.crakac.ofuton;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class SettingActivity extends PreferenceActivity {

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setFontSize();
        setDateDisplayMode();
        setInlinePreview();
        displayVersionInfo();
        setStatusTween();
        authorsWebsite();
    }

    @SuppressWarnings("deprecation")
    private void setInlinePreview() {
        CheckBoxPreference inlinePref = (CheckBoxPreference) findPreference(getString(R.string.show_image_in_timeline));
        inlinePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ReloadChecker.requestSoftReload(true);
                return true;
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void setFontSize() {
        ListPreference fontPref;
        fontPref = (ListPreference) findPreference(getString(R.string.font_size));
        fontPref.setSummary(fontPref.getValue());
        fontPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((CharSequence) newValue);
                ReloadChecker.requestSoftReload(true);
                return true;
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void setDateDisplayMode() {
        ListPreference datePref;
        datePref = (ListPreference) findPreference(getString(R.string.date_display_mode));
        datePref.setSummary(datePref.getValue());
        datePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((CharSequence) newValue);
                return true;
            }
        });
    }

    /**
     * バージョン情報を表示する
     */
    @SuppressWarnings("deprecation")
    private void displayVersionInfo() {
        String versionName = null;
        PackageManager packageManager = this.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(this.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            versionName = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        PreferenceScreen versionInfo;
        versionInfo = (PreferenceScreen) findPreference(getString(R.string.version_info));
        versionInfo.setSummary(versionName != null ? versionName : "取得に失敗しました");
    }

    private void setStatusTween(){
        CheckBoxPreference inlinePref = (CheckBoxPreference) findPreference(getString(R.string.add_animation));
        inlinePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ReloadChecker.requestHardReload(true);
                return true;
            }
        });
    }

    /**
     * 作者のページへ飛ぶ
     */
    @SuppressWarnings("deprecation")
    private void authorsWebsite() {
        PreferenceScreen gotoWeb;
        gotoWeb = (PreferenceScreen) findPreference(getString(R.string.goto_webpage));
        gotoWeb.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.homepage))));
                return true;
            }
        });
    }
}
