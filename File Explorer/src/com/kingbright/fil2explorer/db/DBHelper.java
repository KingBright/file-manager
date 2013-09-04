
package com.kingbright.fil2explorer.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.kingbright.fil2explorer.extention.Extentions;

public class DBHelper {
    public static final int ALREADY_ADD = 1;
    public static final int ADD_FAIL = 2;
    public static final int ADD_SUCCESS = 3;

    public static int addFavourite(Context context, File file) {
        try {
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(FileProvider.CONTENT_URI, new String[] {
                    FileProvider.FAVOURITE
            }, FileProvider.PATH + " = ?", new String[] {
                    file.getPath()
            }, null);
            if (c.getCount() == 0) {
                ContentValues cv = new ContentValues();
                cv.put(FileProvider.NAME, file.getName());
                cv.put(FileProvider.PATH, file.getPath());
                cv.put(FileProvider.FAVOURITE, FileProvider.FLAG_FAVOURITE);
                cv.put(FileProvider.TYPE, Extentions.getIntance(context).getType(file));
                if (cr.insert(FileProvider.CONTENT_URI, cv) != null) {
                    return ADD_SUCCESS;
                }
            }
            c.moveToFirst();
            int favourite = c.getInt(c.getColumnIndex(FileProvider.FAVOURITE));
            if (favourite == FileProvider.FLAG_FAVOURITE) {
                return ALREADY_ADD;
            } else {
                ContentValues cv = new ContentValues();
                cv.put(FileProvider.FAVOURITE, FileProvider.FLAG_FAVOURITE);
                int success = cr.update(FileProvider.CONTENT_URI, cv, FileProvider.PATH + " = "
                        + file.getName(),
                        null);
                if (success > 0) {
                    return ADD_SUCCESS;
                }
                return ADD_FAIL;
            }

        } catch (Exception e) {
            return ADD_FAIL;
        }
    }

    public static void removeFavourite(Context context, File file) {

    }

    public static List<File> getFavourites(Context context) {
        try {
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(FileProvider.CONTENT_URI, new String[] {
                    FileProvider.PATH
            }, FileProvider.FAVOURITE + " = " + FileProvider.FLAG_FAVOURITE, null, null);
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                List<File> list = new ArrayList<File>();
                do {
                    String path = c.getString(c.getColumnIndex(FileProvider.PATH));
                    File file = new File(path);
                    if (file.exists()) {
                        list.add(file);
                    }
                } while (c.moveToNext());

                return list;
            }
        } catch (Exception e) {
        }
        return null;
    }
}
