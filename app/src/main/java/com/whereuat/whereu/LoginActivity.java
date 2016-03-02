package com.whereuat.whereu;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Button b;
    EditText ed;

    TextView tx1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        b=(Button)findViewById(R.id.button);
        ed=(EditText)findViewById(R.id.editText);


        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              login();
            }
        });
    }

    public void login(){
        Log.d(TAG, "Login");


        b.setEnabled(false);

//        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
//                R.style.AppTheme_Dark_Dialog);
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Profile...");
        progressDialog.show();

        String phone_num = ed.getText().toString();

        // TODO: Implement your own authentication logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
//                        onLoginSuccess();

                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

