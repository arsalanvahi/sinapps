package com.app.beautiful_mind;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView totalUsageTextView;
    private PieChart socialAppChart;
    private TextView sleepWakeTextView;
    private LinearLayout socialAppsList;

    private static final String PREFS_NAME = "WakeSleepPrefs";
    private static final String KEY_SLEEP_WAKE_INFO = "sleepWakeInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        totalUsageTextView = findViewById(R.id.totalUsageTextView);
        socialAppChart = findViewById(R.id.socialAppChart);
        sleepWakeTextView = findViewById(R.id.sleepWakeTextView);
        socialAppsList = findViewById(R.id.socialAppsList);
        Button otherAppsButton = findViewById(R.id.otherAppsButton);

        // Request permissions
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission();
        } else {
            initializeAppFeatures();
        }

        otherAppsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, OtherAppsActivity.class);
            startActivity(intent);
        });


        getWakeSleepTime();

    }



    private void initializeAppFeatures() {
        // Display total usage
        long totalUsage = UsageTracker.getTotalUsage(this);
        totalUsageTextView.setText(formatTimeLong(totalUsage));

        // Populate Social App Chart
        Map<String, Long> socialAppUsage = UsageTracker.getSocialAppUsage(this);
        if (!socialAppUsage.isEmpty()) {
            populatePieChart(socialAppChart, socialAppUsage, String.valueOf(R.string.social_apps_usage));
            displaySocialApps(socialAppUsage);
        } else {
            socialAppChart.setNoDataText(String.valueOf(R.string.no_social_apps_usage_found));
            socialAppsList.removeAllViews(); // Clear the list if no data
            TextView noDataText = new TextView(this);
            noDataText.setText(R.string.no_social_apps_data_available);
            socialAppsList.addView(noDataText);
        }

    }

    private void populatePieChart(PieChart chart, Map<String, Long> usageData, String chartLabel) {
        List<PieEntry> entries = new ArrayList<>();
        PackageManager pm = getPackageManager();

        for (Map.Entry<String, Long> entry : usageData.entrySet()) {
            String packageName = entry.getKey();
            long time = entry.getValue();

            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                String appName = pm.getApplicationLabel(appInfo).toString();
                entries.add(new PieEntry(time, appName));
            } catch (PackageManager.NameNotFoundException e) {
                entries.add(new PieEntry(time, packageName)); // fallback
            }
        }

        // Define dynamic colors
        List<Integer> colors = new ArrayList<>();
        for (Map.Entry<String, Long> entry : usageData.entrySet()) {
            switch (entry.getKey()) {
                case "com.facebook.katana":
                    colors.add(ContextCompat.getColor(this, R.color.colorFacebook));
                    break;
                case "com.whatsapp":
                    colors.add(ContextCompat.getColor(this, R.color.colorWhatsApp));
                    break;
                case "com.instagram.android":
                    colors.add(ContextCompat.getColor(this, R.color.colorInstagram));
                    break;
                case "org.telegram.messenger":
                    colors.add(ContextCompat.getColor(this, R.color.colorTelegram));
                    break;
                default:
                    colors.add(ContextCompat.getColor(this, R.color.purple_700));
                    break;
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, chartLabel);
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setHoleRadius(50f);
        chart.invalidate();
    }

    private void displaySocialApps(Map<String, Long> socialAppUsage) {
        socialAppsList.removeAllViews();

        PackageManager pm = getPackageManager();
        for (Map.Entry<String, Long> entry : socialAppUsage.entrySet()) {
            String packageName = entry.getKey();
            long time = entry.getValue();

            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                String appName = pm.getApplicationLabel(appInfo).toString();
                Drawable appIcon = pm.getApplicationIcon(packageName);

                View appLayout = getLayoutInflater().inflate(R.layout.social_app_item, null);

                TextView appNameTextView = appLayout.findViewById(R.id.appNameTextView);
                TextView appUsageTextView = appLayout.findViewById(R.id.appUsageTextView);
                ImageView appIconView = appLayout.findViewById(R.id.appIconView);
                ImageView nextIconView = appLayout.findViewById(R.id.nextIconView);

                appNameTextView.setText(appName);
                appUsageTextView.setText(formatTime(time));
                appIconView.setImageDrawable(appIcon);

                nextIconView.setOnClickListener(v -> openAppDetailsActivity(packageName, appName));
                appLayout.setOnClickListener(v -> openAppDetailsActivity(packageName, appName));

                socialAppsList.addView(appLayout);
            } catch (PackageManager.NameNotFoundException e) {
                // Handle exceptions
            }
        }
    }

    private void openAppDetailsActivity(String packageName, String appName) {
        Intent intent = new Intent(this, AppDetailsActivity.class);
        intent.putExtra("PACKAGE_NAME", packageName);
        intent.putExtra("APP_NAME", appName);
        startActivity(intent);
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestUsageStatsPermission() {
        Toast.makeText(this, R.string.grant_usage_access_permission, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String formatTimeLong(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        StringBuilder result = new StringBuilder();
        if (days > 0) result.append(days).append(" days, ");
        if (hours > 0) result.append(hours).append(" hours, ");
        if (minutes > 0) result.append(minutes).append(" minutes, ");
        result.append(seconds).append(" seconds");
        return result.toString();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (hasUsageStatsPermission()) {
            initializeAppFeatures();
        } else {
            requestUsageStatsPermission();
        }

        getWakeSleepTime();

    }



    private void getWakeSleepTime() {
        long lastUnlockTime = LockUnlockTimeFetcher.getLastUnlockTime(this);
        long lastLockTime = LockUnlockTimeFetcher.getLastLockTime(this);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        // Format lock and unlock times into human-readable format
        String wakeTime = lastUnlockTime != 0 ? sdf.format(new Date(lastUnlockTime)) : "N/A";
        String sleepTime = lastLockTime != 0 ? sdf.format(new Date(lastLockTime)) : "N/A";

        // Calculate if unlock time is two hours after lock time
        if (lastUnlockTime != 0 && lastLockTime != 0) {
            long difference = lastUnlockTime - lastLockTime; // Difference in milliseconds
            long twoHoursInMillis = 2 * 60 * 60 * 1000; // 2 hours in milliseconds

            String sleepWakeInfo = String.format("Wake: %s | Sleep: %s", wakeTime, sleepTime);
            Log.d("MainActivity", "sleepWakeInfo: " + sleepWakeInfo);


            if (difference >= twoHoursInMillis) {
                Log.d("MainActivity", "Unlock time is at least two hours later than lock time.");

                // Save sleep and wake info to SharedPreferences
                saveSleepWakeInfo(sleepWakeInfo);

                // Update TextView
                sleepWakeTextView.setText(sleepWakeInfo);
            } else {
                Log.d("MainActivity", "Unlock time is less than two hours after lock time.");

                // Load previously saved info
                String savedInfo = loadSleepWakeInfo();
                sleepWakeTextView.setText(savedInfo);
            }
        } else {
            Log.d("MainActivity", "Lock or unlock time is not available to compare.");

            // Load previously saved info as fallback
            String savedInfo = loadSleepWakeInfo();
            sleepWakeTextView.setText(savedInfo);
        }
    }


    private void saveSleepWakeInfo(String info) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_SLEEP_WAKE_INFO, info);
        editor.apply();
        Log.d("MainActivity", "Saved sleep/wake info: " + info);
    }


    private String loadSleepWakeInfo() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String info = prefs.getString(KEY_SLEEP_WAKE_INFO, "No data available");
        Log.d("MainActivity", "Loaded sleep/wake info: " + info);
        return info;
    }


}
