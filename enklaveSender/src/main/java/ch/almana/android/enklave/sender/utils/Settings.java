package ch.almana.android.enklave.sender.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.preference.PreferenceManager;


public class Settings {

    private static final String PREF_KEY_DEBUG = "debug_mode";
    private static final String PREF_MAP_SATELIT = "PREF_MAP_SATELIT";
    private static Settings instance = null;
    private final Context ctx;

    // hide constructor use getinstance
    private Settings(final Context context) {
        super();
        this.ctx = context.getApplicationContext();
    }

    /**
     * Get the singleton instance
     *
     * @param ctx as from <code>getContext()</code>
     * @return the singleton instance of {@link Settings}
     */
    public static Settings getInstance(final Context ctx) {
        if (instance == null) {
            instance = new Settings(ctx);
        }
        return instance;
    }

    public static Settings getInstance() {
        return instance;
    }

    public SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    private SharedPreferences getLocalPreferences(final String type) {
        return ctx.getSharedPreferences(type, 0);
    }

    /**
     * Get the version of the App
     *
     * @return <code>versionName</code> from {@link PackageInfo}
     */
    public String getVersionName() {
        try {
            PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            return pInfo.versionName;
        } catch (NameNotFoundException e) {
            Logger.i("Cannot get cpu tuner version", e);
        }
        return "";
    }

    private String getStringPreference(final int prefKey, final String defValue) {
        return getPreferences().getString(ctx.getString(prefKey), defValue);
    }

    private boolean getBooleanPreference(final int prefKey, final boolean defValue) {
        return getPreferences().getBoolean(ctx.getString(prefKey), defValue);
    }

    private int getIntPreference(final int prefKey, final int i) {
        try {
            return Integer.parseInt(getStringPreference(prefKey, Integer.toString(i)));
        } catch (NumberFormatException e) {
            Logger.v("Preference not a integer", e);
        }
        return i;
    }

    private float getFloatPreference(final int prefKey, final float f) {
        try {
            return Float.parseFloat(getStringPreference(prefKey, Float.toString(f)));
        } catch (NumberFormatException e) {
            Logger.v("Preference not a float", e);
        }
        return f;
    }

    /**
     * Check if the holo theme is available
     *
     * @return if {@link Build}<code>.VERSION.SDK_INT</code> is bigger or equal
     * as {@link Build}<code>.VERSION_CODES.HONEYCOMB</code>
     */
    public boolean hasHoloTheme() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public boolean isJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public boolean isKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public boolean isHasLongClick() {
        return Build.VERSION.SDK_INT >= 18;
    }

    public boolean enableDebugOption() {
        return Debug.isUnsinedPackage(ctx);
    }

    public void setDebugMode(boolean debugMode) {
        getPreferences().edit().putBoolean(PREF_KEY_DEBUG, debugMode).commit();
    }

    public boolean isDebugMode() {
        return getPreferences().getBoolean(PREF_KEY_DEBUG, enableDebugOption());
    }

    public void setMapSatelitMode(boolean mapSat) {
        getPreferences().edit().putBoolean(PREF_MAP_SATELIT, mapSat).commit();
    }

    public boolean isMapSatelit(){
        return getPreferences().getBoolean(PREF_MAP_SATELIT, true);
    }
}
