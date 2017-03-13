package com.lady.viktoria.lightdrip.scheduler;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

public class BackupSyncJob extends Job {

    public static final String TAG = "job_backup_tag";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        // run your job here
        return Job.Result.SUCCESS;
    }

    public static void scheduleJob() {
        new JobRequest.Builder(BackupSyncJob.TAG)
                .setExecutionWindow(30_000L, 40_000L)
                .build()
                .schedule();
    }
}