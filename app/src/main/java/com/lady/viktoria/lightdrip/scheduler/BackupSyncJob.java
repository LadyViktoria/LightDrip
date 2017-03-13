package com.lady.viktoria.lightdrip.scheduler;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.lady.viktoria.lightdrip.services.DriveBackupService;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class BackupSyncJob extends Job {

    public static final String TAG = "job_backup_tag";

    public static void schedule() {
        schedule(true);
    }

    private static void schedule(boolean updateCurrent) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 8 AM - 8:10 AM, ignore seconds
        long startMs = TimeUnit.MINUTES.toMillis(60 - minute) + TimeUnit.HOURS.toMillis((24 - hour) % 24);
        long endMs = startMs + (10 * 60 * 1000);

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
            schedule(false); // don't update current, it would cancel this currently running job
        }
    }
}