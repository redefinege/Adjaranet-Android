package helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesManager {
    private static final String PREF_FIRST_SERIES_LAUNCH = "first_series_launch";

    private static PreferencesManager sInstance;
    private SharedPreferences mPreferences;

    private PreferencesManager(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void init(Context context) {
        sInstance = new PreferencesManager(context);
    }

    public static PreferencesManager getInstance() {
        return sInstance;
    }

    public boolean isFirstSeriesLaunch() {
        return mPreferences.getBoolean(PREF_FIRST_SERIES_LAUNCH, true);
    }

    public void setFirstSeriesLaunched() {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(PREF_FIRST_SERIES_LAUNCH, false);
        editor.apply();
    }

    private SharedPreferences.Editor getEditor() {
        return mPreferences.edit();
    }
}
