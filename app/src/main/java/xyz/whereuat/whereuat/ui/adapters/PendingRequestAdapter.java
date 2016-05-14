package xyz.whereuat.whereuat.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import xyz.whereuat.whereuat.PendingRequestsActivity;
import xyz.whereuat.whereuat.R;
import xyz.whereuat.whereuat.ui.views.LatoTextView;

/**
 * This class is an adapter for creating list items in the Pending Requests page.
 */
public class PendingRequestAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private PendingRequestsActivity.MockContact[] mData;

    public PendingRequestAdapter(Context context, PendingRequestsActivity.MockContact[] data) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    public int getCount() { return mData.length; }

    public Object getItem(int position) { return mData[position]; }

    public long getItemId(int position) { return position; }

    public View getView(int position, View convert_view, ViewGroup parent) {
        if (convert_view == null) {
           convert_view = mInflater.inflate(R.layout.pending_req_list_item, parent, false);
        }

        if (mData[position].name() == null) {
            ((LatoTextView) convert_view.findViewById(R.id.pending_req_contact_name)).setText(
                    mData[position].number());
        } else {
            ((LatoTextView) convert_view.findViewById(R.id.pending_req_contact_name)).setText(
                    mData[position].name());
            ((LatoTextView) convert_view.findViewById(R.id.pending_req_contact_number)).setText(
                    mData[position].number());
        }

        // Set up the click listeners for the buttons.
        (convert_view.findViewById(R.id.add_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Adding contact...", Toast.LENGTH_SHORT).show();
            }
        });
        (convert_view.findViewById(R.id.delete_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Deleting request...", Toast.LENGTH_SHORT).show();
            }
        });
        return convert_view;
    }
}
