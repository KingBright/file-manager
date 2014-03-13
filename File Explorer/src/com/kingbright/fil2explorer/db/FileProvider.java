
package com.kingbright.fil2explorer.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class FileProvider extends ContentProvider {

    public static final String PROVIDER_NAME = "com.kingbright.fil2explorer.fileprovider";

    public static final Uri CONTENT_URI = Uri.parse("content://"
            + PROVIDER_NAME + "/files");

    public static final String _ID = "_id";
    public static final String NAME = "name";
    public static final String PATH = "fullpath";
    public static final String FAVOURITE = "favourite";
    public static final String TYPE = "type";

    public static final int FLAG_NOT_FAVOURITE = 0;
    public static final int FLAG_FAVOURITE = 1;

    private static final int FILES = 1;
    private static final int FILE_ID = 2;
    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(PROVIDER_NAME, "files", FILES);
        mUriMatcher.addURI(PROVIDER_NAME, "files/", FILE_ID);
    }

    // ---for database use---
    private SQLiteDatabase mFileDB;
    private static final String DATABASE_NAME = "filedb";
    private static final String DATABASE_TABLE = "files";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE = "create table "
            + DATABASE_TABLE + " (_id integer primary key autoincrement, "
            + NAME + " text not null, " + PATH + " text not null, " + FAVOURITE
            + " integer not null, " + TYPE + " text not null);";

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        mFileDB = dbHelper.getWritableDatabase();
        return (mFileDB == null) ? false : true;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("Content provider database",
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS titles");
            onCreate(db);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(DATABASE_TABLE);

        if (sortOrder == null || sortOrder == "")
            sortOrder = NAME;

        Cursor c = sqlBuilder.query(mFileDB, projection, selection,
                selectionArgs, null, null, sortOrder);

        // ---register to watch a content URI for changes---
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // ---add a new book---
        long rowID = mFileDB.insert(DATABASE_TABLE, "", values);

        // ---if added successfully---
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        int count = 0;
        switch (mUriMatcher.match(arg0)) {
            case FILES: {
                count = mFileDB.delete(DATABASE_TABLE, arg1, arg2);
                break;
            }
            case FILE_ID: {
                String id = arg0.getPathSegments().get(1);
                count = mFileDB.delete(DATABASE_TABLE, _ID + " = " + id
                        + (!TextUtils.isEmpty(arg1) ? " AND (" + arg1 + ')' : ""),
                        arg2);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + arg0);
        }

        getContext().getContentResolver().notifyChange(arg0, null);
        return count;

    }

    @Override
    public int update(Uri uri, ContentValues values,
            String selection, String[] selectionArgs)
    {
        int count = 0;
        switch (mUriMatcher.match(uri)) {
            case FILES: {
                count = mFileDB.update(
                        DATABASE_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            case FILE_ID: {
                count = mFileDB.update(DATABASE_TABLE, values, _ID
                        + " = "
                        + uri.getPathSegments().get(1)
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection
                                + ')' : ""), selectionArgs);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }
}
