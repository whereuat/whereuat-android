package xyz.whereuat.whereuat.db.command;

import android.content.Context;
import android.database.Cursor;

/**
 * <p>Database command for querying entries in a table</p>
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

    /**
     * Constructor for the QueryCommand
     *
     * @param context The Context for the database helper to be created in
     * @param table The name of the table to be execute the command on
     * @param distinct Boolean value for whether or not the queried objects should be distinct
     * @param columns Columns the query should return, a value of null returns all columns
     * @param selection SELECT clause of the SQL statement
     * @param sel_args Arguments to the SELECT clause of the SQL statement, used for SELECT clauses
     *                 that use the ? notation
     * @param group_by GROUP BY clause of the SQL statement
     * @param having HAVING cluase of the SQL statement
     * @param order_by ORDER BY clause of the SQL statement
     * @param limit LIMIT clause of the SQL statement
     *
     * @see android.database.sqlite.SQLiteDatabase#query(boolean, String, String[], String,
     *      String[], String, String, String, String)
     */
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

    /**
     * Workhorse execute method for QueryCommand. Queries the table for all entries that match the
     * given SELECT clause
     *
     * @return Cursor pointing to a set of entries with columns specified by the input
     */
    @Override
    public Cursor call() {
        return mDb.query(mDistinct, mTable, mColumns, mSelection, mSelArgs, mGroupBy, mHaving,
                             mOrderBy, mLimit);
    }
}
