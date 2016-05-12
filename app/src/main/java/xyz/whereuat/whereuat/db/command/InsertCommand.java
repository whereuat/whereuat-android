package xyz.whereuat.whereuat.db.command;

import android.content.ContentValues;
import android.content.Context;

/**
 * <p>Database command for inserting entries into a table</p>
 */
public class InsertCommand extends DbCommand {
    private String mNullColumn;
    private ContentValues mValues;

    /**
     * Constructor for the InsertCommand
     *
     * @param context The Context for the database helper to be created in
     * @param table The name of the table to be execute the command on
     * @param null_column Column to act as a placeholder for a null value so that a row is inserted
     *                    even if the insertion itself is null. A value of null means the row will
     *                    not be inserted if the values are null (Hacky Android implementation
     *                    thing)
     * @param values Column-value pairs to be inserted with the new row
     *
     * @see android.database.sqlite.SQLiteDatabase#insert(String, String, ContentValues)
     */
    public InsertCommand(Context context, String table,
                         String null_column, ContentValues values) {
        super(context, table);
        mNullColumn = null_column;
        mValues = values;
    }

    /**
     * Workhorse execute method for InsertCommand. Inserts the entry from the table defined by the
     * content values.
     *
     * @return Long representing the id of the row that was inserted
     */
    @Override
    public Long call() {
        return mDb.insert(mTable, mNullColumn, mValues);
    }
}
