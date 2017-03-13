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
        int hour = 24 - (appPreferences.getInt("backup_scheduler_time_hour", 1));
        int minute = 60 - (appPreferences.getInt("backup_scheduler_time_minute", 1));
        long startMs = TimeUnit.MINUTES.toMillis(60 - minute) + TimeUnit.HOURS.toMillis((24 - hour) % 24);
        long endMs = startMs + (10 * 60 * 1000);

        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(startMs),
                TimeUnit.MILLISECONDS.toMinutes(startMs) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(startMs) % TimeUnit.MINUTES.toSeconds(1));
        System.out.println(hms);

        String hms2 = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(endMs),
                TimeUnit.MILLISECONDS.toMinutes(endMs) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(endMs) % TimeUnit.MINUTES.toSeconds(1));
        System.out.println(hms2);

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