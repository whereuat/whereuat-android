package xyz.whereuat.whereuat;

import android.os.Bundle;
import android.widget.ListView;

import xyz.whereuat.whereuat.ui.adapters.PendingRequestAdapter;

/**
 * This class manages the activity for displaying and interacting with the Pending Requests page.
 */
public class PendingRequestsActivity extends DrawerActivity {
    // TODO (julius): Delete this once the database is tied to the adapter.
    MockContact[] test_data = {new MockContact("Raymond Jacobson", "+1 (301) 467 2873"),
            new MockContact("Peter Kang", "+1 (917) 204 4747"),
            new MockContact(null, "+1 (309) 319-0689 +1 (309) 319-0689"),
            new MockContact("Spencer Whitehead", "+1 (947) 772 2888")};

    @Override
    public void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.activity_pending_reqs);
        PendingRequestAdapter mAdapter = new PendingRequestAdapter(this, test_data);
        ((ListView) findViewById(R.id.pending_reqs_list)).setAdapter(mAdapter);

        initDrawer();
    }

    public class MockContact {
        private String name;
        private String number;

        public MockContact(String name, String number) {
            this.name = name;
            this.number = number;
        }

        public String name() { return this.name; }
        public String number() { return this.number; }
    }
}
