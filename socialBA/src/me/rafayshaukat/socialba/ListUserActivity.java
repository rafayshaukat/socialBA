package me.rafayshaukat.socialba;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ListUserActivity extends Activity {

	private String currentUserId;
	private ArrayList<String> names;
	protected ListView usersListView;
	protected ArrayAdapter<String> namesArrayAdapter;
	private ProgressDialog progressDialog;
	private BroadcastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_user);
		
		showSpinner();
		
		currentUserId = ParseUser.getCurrentUser().getObjectId();
		names = new ArrayList<String>();
		
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereNotEqualTo("objectId", currentUserId);
		query.findInBackground(new FindCallback<ParseUser>() {
			
			@Override
			public void done(List<ParseUser> userList, ParseException e) {
				if(e == null) {
					for(int i = 0; i < userList.size(); i++) {
						names.add(userList.get(i).getUsername().toString());
//						Toast.makeText(getApplicationContext(), userList.get(i).getUsername().toString(), Toast.LENGTH_LONG).show();
					}
					
					usersListView = (ListView) findViewById(R.id.usersListView);
					namesArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.user_list_item, names);
					usersListView.setAdapter(namesArrayAdapter);
					
					usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> a, View v, int i, long l) {
							openConversation(names, i);
						}
					});
				} else {
					Toast.makeText(getApplicationContext(), "Error loading user list.", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	public void openConversation(ArrayList<String> names, int position) {
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", names.get(position));
		query.findInBackground(new FindCallback<ParseUser>() {

			@Override
			public void done(List<ParseUser> user, ParseException e) {
				if(e == null) {
					Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
					intent.putExtra("RECIPIENT_ID", user.get(0).toString());
					startActivity(intent);
				} else {
					Toast.makeText(getApplicationContext(), "Error finding that user.", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
    //show a loading spinner while the sinch client starts
    private void showSpinner() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Boolean success = intent.getBooleanExtra("success", false);
                progressDialog.dismiss();
                if (!success) {
                    Toast.makeText(getApplicationContext(), "Messaging service failed to start", Toast.LENGTH_LONG).show();
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("me.rafayshaukat.socialba.ListUserActivity"));
    }
}
