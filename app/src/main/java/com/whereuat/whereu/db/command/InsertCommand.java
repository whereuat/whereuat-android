package com.whereuat.whereu.db.command;

import android.content.ContentValues;
import android.content.Context;

/**
 * Created by kangp3 on 4/5/16.
 */
public class InsertCommand extends DbCommand {
    private String mNullColumn;
    private ContentValues mValues;

    public InsertCommand(Context context, String table,
                         String null_column, ContentValues values) {
        super(context, table);
        mNullColumn = null_column;
        mValues = values;
    }

    @Override
    public Long execute() {
        return mDb.insert(mTable, mNullColumn, mValues);
    }
}
