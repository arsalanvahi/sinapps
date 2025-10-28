package com.app.beautiful_mind;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.TypedValue;

public class AppUtils {

    public static String getAppNameFromPackage(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return packageName; // Fallback to package name if app name is not found
        }
    }

    public static int getColorFromTheme(Context context,int colorAttr) {
        // Use TypedValue to retrieve theme color
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(colorAttr, typedValue, true);
        return typedValue.data;
    }
}
