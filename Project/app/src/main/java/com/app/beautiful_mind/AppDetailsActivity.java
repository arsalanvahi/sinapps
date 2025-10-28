package com.app.beautiful_mind;

import static com.app.beautiful_mind.AppUtils.getColorFromTheme;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppDetailsActivity extends AppCompatActivity {

    private ImageView appIconView;
    private TextView appNameTextView, todayUsageTextView, totalUsageTextView;
    private LineChart usageChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);


        // Retrieve app details from intent
        String packageName = getIntent().getStringExtra("PACKAGE_NAME");
        String appName = getIntent().getStringExtra("APP_NAME");


        // Set up the Toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.app_details_activity_name);
        }



        // Initialize views
        appIconView = findViewById(R.id.appIconView);
        todayUsageTextView = findViewById(R.id.todayUsageTextView);
        totalUsageTextView = findViewById(R.id.totalUsageTextView);
        appNameTextView = findViewById(R.id.appNameTextView);
        usageChart = findViewById(R.id.usageChart);

        // Display app details
        displayAppDetails(packageName, appName);
        displayUsageChart(packageName);
    }

    private void displayAppDetails(String packageName, String appName) {
        PackageManager packageManager = getPackageManager();

        try {
            // Get app icon
            Drawable appIcon = packageManager.getApplicationIcon(packageName);

            // Set app name and icon
            appNameTextView.setText(appName);
            appIconView.setImageDrawable(appIcon);

            // Get usage data
            long todayUsage = UsageTracker.getTodayUsageForApp(this, packageName);
            long totalUsage = UsageTracker.getTotalUsageForApp(this, packageName);


            String todayUsageStr = String.valueOf(R.string.today_usage);
            String totalUsageStr = String.valueOf(R.string.total_usage);

            todayUsageTextView.setText(String.format(todayUsageStr + " %s", formatTime(todayUsage)));
            totalUsageTextView.setText(String.format(totalUsageStr + " %s", formatTime(totalUsage)));

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void displayUsageChart(String packageName) {
        // Fetch usage for the last 7 days
        Map<String, Long> usageData = UsageTracker.getAppUsageForLast7Days(this, packageName);
        List<Entry> entries = new ArrayList<>();

        int dayCounter = 0;
        for (Map.Entry<String, Long> entry : usageData.entrySet()) {
            dayCounter++;
            entries.add(new Entry(dayCounter, entry.getValue() / (60 * 1000))); // Time in minutes
        }

        // Get colors from the current theme
        int accentColor = getColorFromTheme(this, androidx.appcompat.R.attr.colorAccent);
        int primaryColor = getColorFromTheme(this, androidx.appcompat.R.attr.colorPrimary);

        // Set up chart dataset
        LineDataSet dataSet = new LineDataSet(entries, String.valueOf(R.string.last_7_days_usage));
        dataSet.setColor(accentColor);
        dataSet.setCircleColor(primaryColor);
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(12f);

        // Configure chart
        LineData lineData = new LineData(dataSet);
        usageChart.setData(lineData);
        usageChart.getDescription().setEnabled(false);

        XAxis xAxis = usageChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);

        usageChart.invalidate(); // Refresh chart
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
