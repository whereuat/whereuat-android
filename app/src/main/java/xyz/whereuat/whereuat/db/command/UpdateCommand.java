package xyz.whereuat.whereuat.db.command;

import android.content.ContentValues;
import android.content.Context;

/**
 * Created by kangp3 on 4/5/16.
 */
public class UpdateCommand extends DbCommand {
    private ContentValues mValues;
    private String mWhere;
    private String[] mWhereArgs;

    public UpdateCommand(Context context, String table,
                         ContentValues values, String where, String[] where_args) {
        super(context, table);
        mValues = values;
        mWhere = where;
        mWhereArgs = where_args;
    }

    @Override
    public Integer execute() {
        return mDb.update(mTable, mValues, mWhere, mWhereArgs);
    }
}
