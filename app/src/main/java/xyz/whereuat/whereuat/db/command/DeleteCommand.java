package xyz.whereuat.whereuat.db.command;

import android.content.Context;

/**
 * <p>Database command for deleting entries from a table</p>
 */
public class DeleteCommand extends DbCommand {
    private String mWhere;
    private String[] mWhereArgs;

    /**
     * Constructor for the DeleteCommand
     *
     * @param context The Context for the database helper to be created in
     * @param table The name of the table to be execute the command on
     * @param where WHERE clause of the SQL statement
     * @param where_args Arguments to the WHERE clause of the SQL statement, used for WHERE
     *                   clauses that use the ? notation
     *
     * @see android.database.sqlite.SQLiteDatabase#delete(String, String, String[])
     */
    public DeleteCommand(Context context, String table,
                         String where, String[] where_args) {
        super(context, table);
        mWhere = where;
        mWhereArgs = where_args;
    }

    /**
     * Workhorse execute method for DeleteCommand. Deletes the entry from the table defined by the
     * WHERE clause and its arguments.
     *
     * @return Integer representing the number of rows that were deleted from the table
     */
    @Override
    public Integer call() {
        return mDb.delete(mTable, mWhere, mWhereArgs);
    }
}
