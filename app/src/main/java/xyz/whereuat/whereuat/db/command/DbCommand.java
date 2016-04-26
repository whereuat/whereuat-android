package xyz.whereuat.whereuat.db.command;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.CallSuper;

import xyz.whereuat.whereuat.db.WhereuatDbHelper;

/**
 * <p>This is the abstract class extended by all of the other DbCommand types. It provides an
 * implementation framework for DbCommand objects</p>
 */
public abstract class DbCommand {
    protected String mTable;
    protected SQLiteDatabase mDb;

    /**
     * Constructor for the DbCommand. It gets the SQLiteDatabase and stores it into the member
     * variable, and sets the table name. This constructor should be called by its subclasses.
     *
     * @param context The Context for the {@link WhereuatDbHelper} to be created in
     * @param table The name of the table to be execute the command on
     */
    protected DbCommand(Context context, String table) {
        mDb = new WhereuatDbHelper(context).getWritableDatabase();
        mTable = table;
    }

    /**
     * The main executed method of the DbCommand. This is left for the subclasses to implement,
     * and should be the workhorse method for their execution.
     *
     * @return Object storing the result of the database operation
     */
    public abstract Object execute();
}
