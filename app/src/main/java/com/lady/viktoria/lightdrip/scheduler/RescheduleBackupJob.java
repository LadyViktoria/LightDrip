package com.lady.viktoria.lightdrip.scheduler;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;

public class RescheduleBackupJob extends Job {

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        // something strange happened, try again later
        return Job.Result.RESCHEDULE;
    }

    @Override
    protected void onReschedule(int newJobId) {
        // the rescheduled job has a new ID
    }
}