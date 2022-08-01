/*
 * SPDX-FileCopyrightText: 2016, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.safetynet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SafetyNetDatabase extends SQLiteOpenHelper {
    private static final String TAG = SafetyNetDatabase.class.getSimpleName();
    private static final String DB_NAME = "snet.db";
    private static final int DB_VERSION = 1;
    private static final String CREATE_TABLE_RECENTS = "CREATE TABLE recents (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT ," +
            "request_type TEXT," +
            "package_name TEXT," +
            "request_key TEXT," +
            "nonce TEXT," +
            "timestamp INTEGER," +
            "result_status_code INTEGER DEFAULT NULL," +
            "result_status_msg TEXT DEFAULT NULL," +
            "result_data TEXT DEFAULT NULL)";
    private static final String TABLE_RECENTS = "recents";
    private static final String FIELD_ID = "id";
    private static final String FIELD_REQUEST_TYPE = "request_type";
    private static final String FIELD_PACKAGE_NAME = "package_name";
    private static final String FIELD_KEY = "request_key";
    private static final String FIELD_NONCE = "nonce";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_RESULT_STATUS_CODE = "result_status_code";
    private static final String FIELD_RESULT_STATUS_MSG = "result_status_msg";
    private static final String FIELD_RESULT_DATA = "result_data";

    private Context context;

    public SafetyNetDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        if (Build.VERSION.SDK_INT >= 16) {
            this.setWriteAheadLoggingEnabled(true);
        }
        clearOldRequests();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_RECENTS);
    }

    private SafetyNetSummary createSafetyNetSummary(Cursor cursor){

        SafetyNetSummary summary = new SafetyNetSummary(
                SafetyNetRequestType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(FIELD_REQUEST_TYPE))),
                cursor.getString(cursor.getColumnIndexOrThrow(FIELD_PACKAGE_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(FIELD_KEY)),
                cursor.getBlob(cursor.getColumnIndexOrThrow(FIELD_NONCE)),
                cursor.getLong(cursor.getColumnIndexOrThrow(FIELD_TIMESTAMP))
        );
        summary.setId(cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_ID)));

        if(cursor.isNull(cursor.getColumnIndexOrThrow(FIELD_RESULT_STATUS_CODE)))return summary;

        summary.setResultStatus(new Status(
                cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_RESULT_STATUS_CODE)),
                cursor.getString(cursor.getColumnIndexOrThrow(FIELD_RESULT_STATUS_MSG))
        ));
        summary.setResultData(cursor.getString(cursor.getColumnIndexOrThrow(FIELD_RESULT_DATA)));


        return summary;

    }

    public void clearOldRequests(){
        final int timeout = 1000*60*60*24*7; // 7 days

        SQLiteDatabase db = getWritableDatabase();

        // whereArgs (for delete()) do not work with integers
        SQLiteStatement sqLiteStatement = db.compileStatement("DELETE FROM "+TABLE_RECENTS+" WHERE "+FIELD_TIMESTAMP+" + ? < ?");
        sqLiteStatement.bindLong(1, timeout);
        sqLiteStatement.bindLong(2, System.currentTimeMillis());
        int rows = sqLiteStatement.executeUpdateDelete();

        if(rows!=0) Log.d(TAG, "Cleared "+rows+" old request(s)");
    }

    public synchronized List<SafetyNetSummary> getRecentRequestsList() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_RECENTS, null, null, null, null, null, null);
        if (cursor != null) {
            List<SafetyNetSummary> result = new ArrayList<>();
            while (cursor.moveToNext()) {
                result.add(createSafetyNetSummary(cursor));
            }
            cursor.close();
            return result;
        }
        return Collections.emptyList();
    }

    public long insertRecentRequestStart(SafetyNetRequestType requestType, String packageName, String key, byte[] nonce, long timestamp){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(FIELD_REQUEST_TYPE, requestType.name());
        cv.put(FIELD_PACKAGE_NAME, packageName);
        cv.put(FIELD_KEY, key);
        cv.put(FIELD_NONCE, nonce);
        cv.put(FIELD_TIMESTAMP, timestamp);

        return db.insert(TABLE_RECENTS, null, cv);
    }

    public void insertRecentRequestEnd(long id, Status status, String resultData){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(FIELD_RESULT_STATUS_CODE, status.getStatusCode());
        cv.put(FIELD_RESULT_STATUS_MSG, status.getStatusMessage());
        cv.put(FIELD_RESULT_DATA, resultData);

        db.update(TABLE_RECENTS, cv, FIELD_ID+" = ?", new String[]{String.valueOf(id)});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("Upgrades not supported");
    }

}
