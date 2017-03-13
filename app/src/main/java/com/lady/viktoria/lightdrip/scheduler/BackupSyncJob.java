package com.lady.viktoria.lightdrip.scheduler;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.lady.viktoria.lightdrip.services.DriveBackupService;
import net.grandcentrix.tray.AppPreferences;
import java.util.concurrent.TimeUnit;

public class BackupSyncJob extends Job {

    public static final String TAG = "job_backup_tag";

    public static void schedule(Context context) {
        schedule(context, true);
    }

    private static void schedule(Context context, boolean updateCurrent) {
        final AppPreferences appPreferences = new AppPreferences(context);
        int spinner = appPreferences.getInt("backup_scheduler_time_day", 0);
        int day = 0;
        if (spinner == 0) day = 0;
        if (spinner == 1) day = 7;
        if (spinner == 2) day = 28;
        int hour = 24 - (appPreferences.getInt("backup_scheduler_time_hour", 1));
        int minute = 60 - (appPreferences.getInt("backup_scheduler_time_minute", 1));
        long startMs = TimeUnit.DAYS.toMillis(day) + TimeUnit.MINUTES.toMillis(60 - minute) + TimeUnit.HOURS.toMillis((24 - hour) % 24);
        long endMs = startMs + (5 * 60 * 1000);

        new JobRequest.Builder(TAG)
                .setExecutionWindow(startMs, endMs)
                .setPersisted(true)
                .setUpdateCurrent(updateCurrent)
                .build()
                .schedule();
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        try {
            Intent mServiceDriveBackupIntent = new Intent(getContext(), DriveBackupService.class);
            startWakefulService(mServiceDriveBackupIntent);
            return Result.SUCCESS;
        } finally {
            schedule(getContext(), false); // don't update current, it would cancel this currently running job
        }
    }
}