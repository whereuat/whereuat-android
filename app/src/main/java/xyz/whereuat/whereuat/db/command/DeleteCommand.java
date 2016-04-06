package xyz.whereuat.whereuat.db.command;

import android.content.Context;

/**
 * Created by kangp3 on 4/5/16.
 */
public class DeleteCommand extends DbCommand {
    private String mWhere;
    private String[] mWhereArgs;

    public DeleteCommand(Context context, String table,
                         String where, String[] where_args) {
        super(context, table);
        mWhere = where;
        mWhereArgs = where_args;
    }

    @Override
    public Integer execute() {
        return mDb.delete(mTable, mWhere, mWhereArgs);
    }
}
