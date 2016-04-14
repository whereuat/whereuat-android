package xyz.whereuat.whereuat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by kangp3 on 4/8/16.
 */
public class KeyLocDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.key_loc_dialog, null);
        builder.setView(view)
               .setMessage(R.string.key_location_prompt_text)
               .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       Dialog d = (Dialog) dialog;
                       EditText e = (EditText) d.findViewById(R.id.key_loc_name_input);

                       String name = e.getText().toString();
                       Location loc = LocationProviderService.getLocation();

                       boolean name_is_valid = KeyLocation.nameIsValid(name),
                               loc_is_valid = KeyLocation.locIsValid(loc);
                       if (name_is_valid && loc_is_valid) {
                           addKeyLoc(activity, name, loc);
                       } else if (!name_is_valid) {
                           String err_str = "Invalid location name";
                           Toast.makeText(activity, err_str, Toast.LENGTH_SHORT).show();
                       } else if (!loc_is_valid) {
                           String err_str = "Location not found";
                           Toast.makeText(activity, err_str, Toast.LENGTH_SHORT).show();
                       }
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {

                   }
               });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog d = (AlertDialog) getDialog();
        d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);

        EditText e = (EditText) d.findViewById(R.id.key_loc_name_input);
        e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    d.getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
    }

    private void addKeyLoc(Context context, String name, Location loc) {
        new KeyLocation(name, loc.getLatitude(), loc.getLongitude()).dbInsert(context);
    }
}
