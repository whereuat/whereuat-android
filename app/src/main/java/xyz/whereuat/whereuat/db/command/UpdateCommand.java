package xyz.whereuat.whereuat.db.command;

import android.content.ContentValues;
import android.content.Context;

/**
 * <p>Database command for updating entries in a table</p>
 */
public class UpdateCommand extends DbCommand {
    private ContentValues mValues;
    private String mWhere;
    private String[] mWhereArgs;

    /**
     * Constructor for the UpdateCommand
     *
     * @param context The Context for the database helper to be created in
     * @param table The name of the table to execute the command on
     * @param values Column-value pairs to be updated in the target rows
     * @param where WHERE clause of the SQL statement
     * @param where_args Arguments to the WHERE clause of the SQL statement, used for WHERE
     *                   clauses that use the ? notation
     */
    public UpdateCommand(Context context, String table,
                         ContentValues values, String where, String[] where_args) {
        super(context, table);
        mValues = values;
        mWhere = where;
        mWhereArgs = where_args;
    }

    /**
     * Workhorse execute method for UpdateCommand. Updates all entries that match the WHERE clause
     * with the specified values
     *
     * @return Integer representing the number of rows that were updated in the table
     */
    @Override
    public Integer call() {
        return mDb.update(mTable, mValues, mWhere, mWhereArgs);
    }
}
