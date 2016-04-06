package com.whereuat.whereu.db.command;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.whereuat.whereu.db.WhereuatDbHelper;

/**
 * Created by kangp3 on 4/5/16.
 */
public abstract class DbCommand {
    protected String mTable;
    protected SQLiteDatabase mDb;

    protected DbCommand(Context context, String table) {
        mDb = new WhereuatDbHelper(context).getWritableDatabase();
        mTable = table;
    }

    public abstract Object execute();
}
