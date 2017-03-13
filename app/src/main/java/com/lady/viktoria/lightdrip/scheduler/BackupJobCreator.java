package com.lady.viktoria.lightdrip.scheduler;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class BackupJobCreator implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag) {
            case BackupSyncJob.TAG:
                return new BackupSyncJob();
            default:
                return null;
        }
    }
}