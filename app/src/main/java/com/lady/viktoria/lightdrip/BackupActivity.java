/*
 * Copyright (C) 2016 Glucosio Foundation
 *
 * This file is part of Glucosio.
 *
 * Glucosio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Glucosio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Glucosio.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.lady.viktoria.lightdrip;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;
import com.lady.viktoria.lightdrip.RealmBackup.Backup;
import com.lady.viktoria.lightdrip.RealmBackup.BackupAdapter;
import com.lady.viktoria.lightdrip.RealmBackup.LightDripBackup;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import net.grandcentrix.tray.AppPreferences;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import io.realm.Realm;

import static io.realm.Realm.getDefaultInstance;

public class BackupActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{
    private static final int REQUEST_CODE_PICKER = 2;
    private static final int REQUEST_CODE_PICKER_FOLDER = 4;

    private final static String TAG = BackupActivity.class.getSimpleName();
    private static final String BACKUP_FOLDER_KEY = "backup_folder";
    private int numberofstoredrecords = 5;

    @BindView(R.id.activity_backup_drive_button_backup) Button backupButton;
    @BindView(R.id.activity_backup_drive_button_manage_drive) Button manageButton;
    @BindView(R.id.activity_backup_drive_textview_folder) TextView folderTextView;
    @BindView(R.id.activity_backup_drive_button_folder) LinearLayout selectFolderButton;
    @BindView(R.id.activity_backup_drive_listview_restore) ExpandableHeightListView backupListView;
    @BindView(R.id.backup_drive_et_numberofrecords) EditText noRecords;
    @BindView(R.id.switch_backup_activity) Switch sButton;
    @BindView(R.id.time_tv_backup_activity) TextView timeTextView;



    private Backup backup;
    private GoogleApiClient mGoogleApiClient;
    private IntentSender intentPicker;
    private Realm realm;
    private String backupFolder;

    AppPreferences appPreferences;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_drive_activity);
        ButterKnife.bind(this);

        MainApplication mainApplication = (MainApplication) getApplicationContext();
        appPreferences = new AppPreferences(this);
        realm = getDefaultInstance();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Backup Database to Google Drive");

        backup = mainApplication.getBackup();
        backup.init(this);
        connectClient();
        mGoogleApiClient = backup.getClient();
        backupListView.setExpanded(true);
        backupButton.setOnClickListener(v -> {openFolderPicker(true);});
        selectFolderButton.setOnClickListener(v -> {openFolderPicker(false);});
        manageButton.setOnClickListener(v -> openOnDrive(DriveId.decodeFromString(backupFolder)));
        numberofstoredrecords = appPreferences.getInt("NUMBER_OF_STORED_RECORDS", 5);
        String schedulerTime = appPreferences.getString("scheduler_time", "Click to set Time");
        timeTextView.setText("Backup at: " + schedulerTime + ":00");

        Boolean useScheduler = appPreferences.getBoolean("use_scheduler", false);
        if (useScheduler) {
            timeTextView.setVisibility(View.VISIBLE);
            sButton.setChecked(true);
        } else {
            timeTextView.setVisibility(View.GONE);
            sButton.setChecked(false);
        }
        noRecords.setText(String.valueOf(numberofstoredrecords));

        // Show backup folder, if exists
        backupFolder = appPreferences.getString(BACKUP_FOLDER_KEY, "");
        if (!("").equals(backupFolder)) {
            setBackupFolderTitle(DriveId.decodeFromString(backupFolder));
            manageButton.setVisibility(View.VISIBLE);
        }

        // Populate backup list
        if (!("").equals(backupFolder)) {
            getBackupsFromDrive(DriveId.decodeFromString(backupFolder).asDriveFolder());
        }
    }

    @OnCheckedChanged(R.id.switch_backup_activity)
    public void checkboxToggled (boolean isChecked) {
        if (isChecked) {
            timeTextView.setVisibility(View.VISIBLE);
            appPreferences.put("use_scheduler", true);
        } else {
            timeTextView.setVisibility(View.GONE);
            appPreferences.put("use_scheduler", false);
        }
    }

    @OnClick(R.id.time_tv_backup_activity)
    public void onClick(View views) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                BackupActivity.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );
        tpd.enableMinutes(false);
        tpd.setThemeDark(true);
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        timeTextView.setText("Backup at: " + hourOfDay + ":00");
        appPreferences.put("scheduler_time", hourOfDay);
    }

    @OnTextChanged(value = R.id.backup_drive_et_numberofrecords,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void noRecordsInput(Editable editable) {
        String value = noRecords.getText().toString();
        if (value.equals("0") || value.equals("")) {
            noRecords.setError("not allowed");
        } else {
            appPreferences.put("NUMBER_OF_STORED_RECORDS", Integer.parseInt(value));
        }
    }

    @OnEditorAction(R.id.backup_drive_et_numberofrecords)
     boolean onEditorAction(TextView v, int actionId, KeyEvent key) {
        if(actionId== EditorInfo.IME_ACTION_DONE){
            InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            noRecords.clearFocus();
        }
        return false;
    }

    private void setBackupFolderTitle(DriveId id) {
        id.asDriveFolder().getMetadata((mGoogleApiClient)).setResultCallback(
                result -> {
                    if (!result.getStatus().isSuccess()) {
                        showErrorDialog();
                        return;
                    }
                    Metadata metadata = result.getMetadata();
                    folderTextView.setText(metadata.getTitle());
                }
        );
    }

    private void openFolderPicker(boolean uploadToDrive) {
        if (uploadToDrive) {
            // First we check if a backup folder is set
            if (TextUtils.isEmpty(backupFolder)) {
                try {
                    if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                        if (intentPicker == null)
                            intentPicker = buildIntent();
                        //Start the picker to choose a folder
                        startIntentSenderForResult(
                                intentPicker, REQUEST_CODE_PICKER, null, 0, 0, 0);
                    }
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Unable to send intent", e);
                    showErrorDialog();
                }
            } else {
                uploadToDrive(DriveId.decodeFromString(backupFolder));
            }
        } else {
            try {
                intentPicker = null;
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    if (intentPicker == null)
                        intentPicker = buildIntent();
                    //Start the picker to choose a folder
                    startIntentSenderForResult(
                            intentPicker, REQUEST_CODE_PICKER_FOLDER, null, 0, 0, 0);
                }
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Unable to send intent", e);
                showErrorDialog();
            }
        }
    }

    private IntentSender buildIntent() {
        return Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[]{DriveFolder.MIME_TYPE})
                .build(mGoogleApiClient);
    }

    private void getBackupsFromDrive(DriveFolder folder) {
        final Activity activity = this;
        SortOrder sortOrder = new SortOrder.Builder()
                .addSortDescending(SortableField.MODIFIED_DATE).build();
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "lightdrip.realm"))
                .addFilter(Filters.eq(SearchableField.TRASHED, false))
                .setSortOrder(sortOrder)
                .build();
        folder.queryChildren(mGoogleApiClient, query)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {

                    private ArrayList<LightDripBackup> backupsArray = new ArrayList<>();

                    @Override
                    public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
                        MetadataBuffer buffer = result.getMetadataBuffer();
                        int size = buffer.getCount();
                        for (int i = 0; i < size; i++) {
                            Metadata metadata = buffer.get(i);
                            DriveId driveId = metadata.getDriveId();
                            Date modifiedDate = metadata.getModifiedDate();
                            long backupSize = metadata.getFileSize();
                            backupsArray.add(new LightDripBackup(driveId, modifiedDate, backupSize));
                        }
                        backupListView.setAdapter(new BackupAdapter(activity, R.layout.activity_backup_drive_restore_item, backupsArray));
                    }
                });
    }

    public void downloadFromDrive(DriveFile file) {
        file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                .setResultCallback(result -> {
                    if (!result.getStatus().isSuccess()) {
                        showErrorDialog();
                        return;
                    }

                    // DriveContents object contains pointers
                    // to the actual byte stream
                    DriveContents contents = result.getDriveContents();
                    InputStream input = contents.getInputStream();

                    try {
                        File file1 = new File(realm.getPath());
                        OutputStream output = new FileOutputStream(file1);
                        try {
                            try {
                                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                                int read;

                                while ((read = input.read(buffer)) != -1) {
                                    output.write(buffer, 0, read);
                                }
                                output.flush();
                            } finally {
                                safeCloseClosable(input);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        safeCloseClosable(input);
                    }
                    // Reboot app
                    ActivityManager actvityManager = (ActivityManager)
                            getApplicationContext().getSystemService( getApplicationContext().ACTIVITY_SERVICE );
                    List<ActivityManager.RunningAppProcessInfo> procInfos = actvityManager.getRunningAppProcesses();
                    for(int pnum = 0; pnum < procInfos.size(); pnum++) {
                        if((procInfos.get(pnum)).processName.contains("com.lady.viktoria.lightdrip:services")) {
                            android.os.Process.killProcess((procInfos.get(pnum).pid));
                        }
                    }
                    System.exit(1);
                });
    }

    private void safeCloseClosable(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {

                        private ArrayList<LightDripBackup> backupsArray = new ArrayList<>();

                        @Override
                        public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
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
                        }
                    });
            //Create the file on GDrive
            Drive.DriveApi.newDriveContents(mGoogleApiClient)
                    .setResultCallback(result -> {
                        if (!result.getStatus().isSuccess()) {
                            Log.e(TAG, "Error while trying to create new file contents");
                            showErrorDialog();
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
                                    inputStream = new FileInputStream(new File(realm.getPath()));
                                } catch (FileNotFoundException e) {
                                    showErrorDialog();
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

                                    showErrorDialog();
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
                                                showErrorDialog();
                                                finish();
                                                return;
                                            }
                                            showSuccessDialog();
                                            finish();
                                        });
                            }
                        }.start();
                    });
        }
    }

    private void openOnDrive(DriveId driveId) {
        driveId.asDriveFolder().getMetadata((mGoogleApiClient)).setResultCallback(
                result -> {
                    if (!result.getStatus().isSuccess()) {
                        showErrorDialog();
                        return;
                    }
                    Metadata metadata = result.getMetadata();
                    String url = metadata.getAlternateLink();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
        );
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    backup.start();
                }
                break;
            // REQUEST_CODE_PICKER
            case 2:
                intentPicker = null;

                if (resultCode == RESULT_OK) {
                    //Get the folder drive id
                    DriveId mFolderDriveId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

                    saveBackupFolder(mFolderDriveId.encodeToString());

                    uploadToDrive(mFolderDriveId);
                }
                break;

            // REQUEST_CODE_SELECT
            case 3:
                if (resultCode == RESULT_OK) {
                    // get the selected item's ID
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

                    DriveFile file = driveId.asDriveFile();
                    downloadFromDrive(file);

                } else {
                    showErrorDialog();
                }
                finish();
                break;
            // REQUEST_CODE_PICKER_FOLDER
            case 4:
                if (resultCode == RESULT_OK) {
                    //Get the folder drive id
                    DriveId mFolderDriveId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

                    saveBackupFolder(mFolderDriveId.encodeToString());
                    // Restart activity to apply changes
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
                break;
        }
    }

    private void saveBackupFolder(String folderPath) {
        appPreferences.put(BACKUP_FOLDER_KEY, folderPath);
    }

    private void showSuccessDialog() {
        Toast.makeText(getApplicationContext(), "backup success", Toast.LENGTH_SHORT).show();
    }

    private void showErrorDialog() {
        Toast.makeText(getApplicationContext(), "backup failed", Toast.LENGTH_SHORT).show();
    }

    public void connectClient() {
        backup.start();
    }

    public void disconnectClient() {
        backup.stop();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectClient();
    }
}
