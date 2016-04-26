package xyz.whereuat.whereuat.db;

import android.os.AsyncTask;

import xyz.whereuat.whereuat.db.command.DbCommand;

/**
 * <p>DbTask is a wrapper around the built-in AsyncTask specifically meant for executing database
 * operations for whereu@ off of the main thread. Callbacks to DbTasks should be implemented by
 * overriding the {{@link #onPostExecute(Object)}} method. The {{@link #execute(Object[])}} method
 * should be called with a {@link DbCommand} input. The result of executing the {@link DbCommand}
 * will then be passed into the {{@link #onPostExecute(Object)}} method, and will need to be
 * type-casted to the correct result type inside of the callback.</p>
 */
public class DbTask extends AsyncTask<DbCommand, Void, Object> {
    /**
     * Default constructor for DbTask objects
     */
    public DbTask() { }

    /**
     * Method that executes asynchronously when the {{@link #execute(Object[])}} command is called.
     *
     * @param commands Command to be executed asynchronously in the DbTask
     * @return Result Object from the command's execute call. This is passed into the
     *         {{@link #onPostExecute(Object)}} method, and will need to be type-casted there.
     */
    @Override
    protected Object doInBackground(DbCommand... commands) {
        DbCommand command = commands[0];
        return command.execute();
    }
}
