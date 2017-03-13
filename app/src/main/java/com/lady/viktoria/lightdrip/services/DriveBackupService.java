package com.lady.viktoria.lightdrip.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;

import net.grandcentrix.tray.AppPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import io.realm.Realm;

import static io.realm.Realm.getDefaultInstance;

public class DriveBackupService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = DriveBackupService.class.getSimpleName();
    private int numberofstoredrecords = 5;

    private GoogleApiClient mGoogleApiClient;
    private Realm mRealm;
    AppPreferences appPreferences;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        appPreferences = new AppPreferences(this);
        Realm.init(this);
        mRealm = getDefaultInstance();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
        String backupFolder = appPreferences.getString("backup_folder", "");
        uploadToDrive(DriveId.decodeFromString(backupFolder));
        return super.onStartCommand(intent, flags, startId);
    }

    private void uploadToDrive(DriveId mFolderDriveId) {
        if (mFolderDriveId != null) {
            final DriveFolder folder = mFolderDriveId.asDriveFolder();
            SortOrder sortOrder = new SortOrder.Builder()
                    .addSortDescending(SortableField.MODIFIED_DATE).build();
            Query query = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.TITLE, "lightdrip.realm"))
                    .addFilter(Filters.eq(SearchableField.TRASHED, false))
                    .setSortOrder(sortOrder)
                    .build();
            folder.queryChildren(mGoogleApiClient, query)
                    .setResultCallback(result -> {
                        MetadataBuffer buffer = result.getMetadataBuffer();
                        numberofstoredrecords = appPreferences.getInt("NUMBER_OF_STORED_RECORDS", 5);
                        int size = buffer.getCount();
                        for (int i = numberofstoredrecords-1; i < size; i++) {
                            Metadata metadata = buffer.get(i);
                            DriveResource driveResource = metadata.getDriveId().asDriveResource();
                            if (metadata.isTrashable()) {
                                if (!metadata.isTrashed()) {
                                    driveResource.trash(mGoogleApiClient);
                                }
                            }
                        }
                    buffer.release();
                    });
            //Create the file on GDrive
            Drive.DriveApi.newDriveContents(mGoogleApiClient)
                    .setResultCallback(result -> {
                        if (!result.getStatus().isSuccess()) {
                            Log.e(TAG, "Error while trying to create new file contents");
                            return;
                        }
                        final DriveContents driveContents = result.getDriveContents();

                        // Perform I/O off the UI thread.
                        new Thread() {
                            @Override
                            public void run() {
                                // write content to DriveContents
                                OutputStream outputStream = driveContents.getOutputStream();

                                FileInputStream inputStream = null;
                                try {
                                    inputStream = new FileInputStream(new File(mRealm.getPath()));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }

                                byte[] buf = new byte[1024];
                                int bytesRead;
                                try {
                                    if (inputStream != null) {
                                        while ((bytesRead = inputStream.read(buf)) > 0) {
                                            outputStream.write(buf, 0, bytesRead);
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                        .setTitle("lightdrip.realm")
                                        .setMimeType("text/plain")
                                        .build();

                                // create a file in selected folder
                                folder.createFile(mGoogleApiClient, changeSet, driveContents)
                                        .setResultCallback(result1 -> {
                                            if (!result1.getStatus().isSuccess()) {
                                                Log.d(TAG, "Error while trying to create the file");
                                            }
                                            mGoogleApiClient.disconnect();
                                        });
                            }
                        }.start();
                    });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}