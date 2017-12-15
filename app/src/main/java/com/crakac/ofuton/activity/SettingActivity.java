package com.crakac.ofuton.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;

import com.crakac.ofuton.R;
import com.crakac.ofuton.util.ReloadChecker;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        FragmentManager fm = getFragmentManager();
        Fragment prefsFragment = fm.findFragmentByTag("prefs");
        if (prefsFragment == null) {
            prefsFragment = new PrefsFragment();
            fm.beginTransaction().replace(R.id.content, prefsFragment, "prefs").commit();
        }
    }

    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            setFontSize();
            setDateDisplayMode();
            setInlinePreview();
            displayLicenseInfo();
            displayVersionInfo();
            requestSoftReloadOnClick(R.string.show_image_in_timeline, R.string.show_source, R.string.date_display_mode);
        }

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
        private void displayVersionInfo() {
            String versionName = null;
            PackageManager packageManager = getActivity().getPackageManager();
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(getActivity().getPackageName(),
                        PackageManager.GET_ACTIVITIES);
                versionName = packageInfo.versionName;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            PreferenceScreen versionInfo;
            versionInfo = (PreferenceScreen) findPreference(getString(R.string.version_info));
            versionInfo.setSummary(versionName != null ? versionName : "取得に失敗しました");
        }

        /**
         * PreferenceScreenにIntentを仕込めなくもないが，パッケージ名を返るといちいち面倒なのでJavaで飛ばす
         */
        private void displayLicenseInfo() {
            PreferenceScreen license = (PreferenceScreen) findPreference(getString(R.string.license_info));
            license.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), LicenseActivity.class));
                    return true;
                }
            });
        }

        private void requestSoftReloadOnClick(int... ids) {
            for (int id : ids) {
                findPreference(getString(id)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        ReloadChecker.requestSoftReload(true);
                        return true;
                    }
                });
            }
        }

    }
}
