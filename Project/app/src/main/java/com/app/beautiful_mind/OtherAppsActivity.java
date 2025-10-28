package com.app.beautiful_mind;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Map;

public class OtherAppsActivity extends AppCompatActivity {
    private LinearLayout otherAppsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_apps);

        otherAppsList = findViewById(R.id.otherAppsList);

        // Set up the Toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.other_apps_activity_name);
        }


        // Display Other Apps Usage
        displayOtherApps();
    }

    private void displayOtherApps() {
        Map<String, Long> otherAppUsage = UsageTracker.getOtherAppUsage(this);
        PackageManager packageManager = getPackageManager();

        otherAppsList.removeAllViews();
        for (Map.Entry<String, Long> entry : otherAppUsage.entrySet()) {
            String packageName = entry.getKey();
            long usageTime = entry.getValue();

            try {
                // Get app information
                ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
                String appName = packageManager.getApplicationLabel(appInfo).toString();
                Drawable appIcon = packageManager.getApplicationIcon(packageName);

                // Inflate the custom view for apps
                View appItem = getLayoutInflater().inflate(R.layout.social_app_item, null);
                TextView appNameTextView = appItem.findViewById(R.id.appNameTextView);
                TextView appUsageTextView = appItem.findViewById(R.id.appUsageTextView);
                ImageView appIconView = appItem.findViewById(R.id.appIconView);
                ImageView nextIconView = appItem.findViewById(R.id.nextIconView);

                // Set app details
                appNameTextView.setText(appName);
                appUsageTextView.setText(formatTime(usageTime));
                appIconView.setImageDrawable(appIcon);

                // Set click listener for next icon
                nextIconView.setOnClickListener(v -> openAppDetailsActivity(packageName, appName));

                // Add click listener to open AppDetailsActivity
                appItem.setOnClickListener(v -> openAppDetailsActivity(packageName, appName));

                otherAppsList.addView(appItem);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void openAppDetailsActivity(String packageName, String appName) {
        Intent intent = new Intent(OtherAppsActivity.this, AppDetailsActivity.class);
        intent.putExtra("PACKAGE_NAME", packageName);
        intent.putExtra("APP_NAME", appName);
        startActivity(intent);
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Go back to the previous activity
        return true;
    }
}
