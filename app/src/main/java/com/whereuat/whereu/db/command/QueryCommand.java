package com.whereuat.whereu.db.command;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by kangp3 on 4/5/16.
 */
public class QueryCommand extends DbCommand {
    private boolean mDistinct;
    private String[] mColumns;
    private String mSelection;
    private String[] mSelArgs;
    private String mGroupBy;
    private String mHaving;
    private String mOrderBy;
    private String mLimit;

    public QueryCommand(Context context, String table,
                        boolean distinct, String[] columns, String selection, String[] sel_args,
                        String group_by, String having, String order_by, String limit) {
        super(context, table);
        mDistinct = distinct;
        mColumns = columns;
        mSelection = selection;
        mSelArgs = sel_args;
        mGroupBy = group_by;
        mHaving = having;
        mOrderBy = order_by;
        mLimit = limit;
    }

    @Override
    public Cursor execute() {
        return mDb.query(mDistinct, mTable, mColumns, mSelection, mSelArgs, mGroupBy, mHaving,
                         mOrderBy, mLimit);
    }
}
