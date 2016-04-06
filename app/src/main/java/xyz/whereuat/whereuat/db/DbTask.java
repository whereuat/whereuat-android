package xyz.whereuat.whereuat.db;

import android.os.AsyncTask;

import xyz.whereuat.whereuat.db.command.DbCommand;

/**
 * Created by kangp3 on 4/5/16.
 */
public class DbTask extends AsyncTask<DbCommand, Void, Object> {
    public interface AsyncResponse {
        void processFinish(Object result);
    }

    public AsyncResponse delegate = null;

    public DbTask() { }
    public DbTask(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Object doInBackground(DbCommand... commands) {
        DbCommand command = commands[0];
        return command.execute();
    }

    @Override
    protected void onPostExecute(Object result) {
        if (delegate != null) {
            delegate.processFinish(result);
        }
    }
}
