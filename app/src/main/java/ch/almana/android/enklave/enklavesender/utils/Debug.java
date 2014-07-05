package ch.almana.android.enklave.enklavesender.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

public class Debug {

    private static final int DEBUG_SIGNATURE_HASH = -1623526495;
    private static boolean checkedUnsiged = false;
    private static boolean isUnsiged = false;

    public static boolean isUnsinedPackage(final Context ctx) {
        if (checkedUnsiged) {
            return isUnsiged;
        }
        String packageName = ctx.getApplicationInfo().packageName;
        try {
            PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            int hash = packageInfo.signatures[0].hashCode();
            if (hash == DEBUG_SIGNATURE_HASH) {
                isUnsiged = true;
                checkedUnsiged = true;
                return true;
            }
        } catch (NameNotFoundException e) {
        }
        isUnsiged = false;
        checkedUnsiged = true;
        return false;
    }

    public static void notImplemented(final Context context) {
        Toast.makeText(context, "Not implemented!", Toast.LENGTH_SHORT).show();
    }
}
