package com.app.beautiful_mind;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsageTracker {

    private static final String[] SOCIAL_APPS = {
            "com.facebook.katana",      // Facebook
            "com.whatsapp",             // WhatsApp
            "com.instagram.android",    // Instagram
            "com.snapchat.android",     // Snapchat
            "org.telegram.messenger"    // Telegram
    };

    // Get total app usage for all apps
    public static Map<String, Long> getTotalAppUsage(Context context) {
        Map<String, Long> totalUsageMap = new HashMap<>();

        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_MONTH, -1); // Past 24 hours
        long startTime = calendar.getTimeInMillis();

        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        if (stats != null) {
            for (UsageStats usageStats : stats) {
                long totalTimeInForeground = usageStats.getTotalTimeInForeground();

                if (totalTimeInForeground > 0) {
                    totalUsageMap.put(usageStats.getPackageName(), totalTimeInForeground);
                }
            }
        }

        return totalUsageMap;
    }

    // Get usage of social apps only
    public static Map<String, Long> getSocialAppUsage(Context context) {
        Map<String, Long> socialUsageMap = new HashMap<>();
        Map<String, Long> totalUsageMap = getTotalAppUsage(context);

        for (String socialApp : SOCIAL_APPS) {
            if (totalUsageMap.containsKey(socialApp)) {
                socialUsageMap.put(socialApp, totalUsageMap.get(socialApp));
            }
        }

        return socialUsageMap;
    }

    // Get usage of non-social apps
    public static Map<String, Long> getOtherAppUsage(Context context) {
        Map<String, Long> otherUsageMap = new HashMap<>();
        Map<String, Long> totalUsageMap = getTotalAppUsage(context);

        for (Map.Entry<String, Long> entry : totalUsageMap.entrySet()) {
            String packageName = entry.getKey();
            if (!isSocialApp(packageName)) {
                otherUsageMap.put(packageName, entry.getValue());
            }
        }

        return otherUsageMap;
    }

    // Helper method to check if an app is a social app
    private static boolean isSocialApp(String packageName) {
        for (String socialApp : SOCIAL_APPS) {
            if (socialApp.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    // Get total usage time for all apps in milliseconds
    public static long getTotalUsage(Context context) {
        long totalUsage = 0;

        Map<String, Long> usageMap = getTotalAppUsage(context);
        for (long time : usageMap.values()) {
            totalUsage += time;
        }

        return totalUsage;
    }


    /**
     * Retrieves today's usage time for a specific app.
     *
     * @param context     Application context
     * @param packageName Package name of the app
     * @return Usage time in milliseconds
     */
    public static long getTodayUsageForApp(Context context, String packageName) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        // Start and end of today
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();

        // Query usage stats
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        long totalUsageTime = 0;

        for (UsageStats usageStats : usageStatsList) {
            if (usageStats.getPackageName().equals(packageName)) {
                totalUsageTime += usageStats.getTotalTimeInForeground();
            }
        }

        return totalUsageTime;
    }

    /**
     * Retrieves the total usage time for a specific app.
     *
     * @param context     Application context
     * @param packageName Package name of the app
     * @return Total usage time in milliseconds
     */
    public static long getTotalUsageForApp(Context context, String packageName) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        // Query usage stats for the last year
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();

        calendar.add(Calendar.YEAR, -1);
        long startTime = calendar.getTimeInMillis();

        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_YEARLY, startTime, endTime);

        long totalUsageTime = 0;

        for (UsageStats usageStats : usageStatsList) {
            if (usageStats.getPackageName().equals(packageName)) {
                totalUsageTime += usageStats.getTotalTimeInForeground();
            }
        }

        return totalUsageTime;
    }


    /**
     * Retrieves the app usage for the last 7 days.
     *
     * @param context     Application context
     * @param packageName Package name of the app
     * @return Map with days as keys (e.g., "Day 1", "Day 2") and usage time in milliseconds
     */
    public static Map<String, Long> getAppUsageForLast7Days(Context context, String packageName) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        Map<String, Long> usageMap = new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();

        for (int i = 0; i < 7; i++) {
            // Set start time to 24 hours ago
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            long startTime = calendar.getTimeInMillis();

            // Query usage stats for the specific day
            List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

            long dayUsageTime = 0;
            for (UsageStats usageStats : usageStatsList) {
                if (usageStats.getPackageName().equals(packageName)) {
                    dayUsageTime += usageStats.getTotalTimeInForeground();
                }
            }

            // Add the day's usage to the map
            usageMap.put("Day " + (7 - i), dayUsageTime);

            // Update endTime for the next iteration
            endTime = startTime;
        }

        return usageMap;
    }


}
