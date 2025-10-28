package com.app.beautiful_mind;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import java.util.Calendar;

public class LockUnlockTimeFetcher {

    private static final String TAG = "LockUnlockTimeFetcher";

    public static long getLastUnlockTime(Context context) {
        return getLastEventTime(context, UsageEvents.Event.SCREEN_INTERACTIVE);
    }

    public static long getLastLockTime(Context context) {
        return getLastEventTime(context, UsageEvents.Event.SCREEN_NON_INTERACTIVE);
    }

    private static long getLastEventTime(Context context, int eventType) {
        UsageStatsManager usageStatsManager =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        // Define the time range for querying usage stats
        long endTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1); // Look back 1 day
        long startTime = calendar.getTimeInMillis();

        // Query for usage events in the specified time range
        UsageEvents events = usageStatsManager.queryEvents(startTime, endTime);

        UsageEvents.Event event;
        long lastEventTime = 0;

        while (events.hasNextEvent()) {
            event = new UsageEvents.Event();
            events.getNextEvent(event);

            // Check for the specified event type
            if (event.getEventType() == eventType) {
                lastEventTime = event.getTimeStamp();
            }
        }

        if (lastEventTime != 0) {
            Log.d(TAG, "Last event (" + eventType + ") time: " + lastEventTime);
        } else {
            Log.d(TAG, "No events of type " + eventType + " found.");
        }

        return lastEventTime;
    }


}
