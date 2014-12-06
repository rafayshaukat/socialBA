package me.rafayshaukat.socialba;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	private Button loginButton;
	private Button signUpButton;
	private EditText usernameField;
	private EditText passwordField;
	protected String username;
	protected String password;
	private Intent intent;
	private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        intent = new Intent(getApplicationContext(), ListUserActivity.class);
        serviceIntent = new Intent(getApplicationContext(), MessageService.class);
        
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser != null) {
            startService(serviceIntent);
            startActivity(intent);
        }
        
        setContentView(R.layout.activity_login);
        
        loginButton = (Button) findViewById(R.id.loginButton);
        signUpButton = (Button) findViewById(R.id.signupButton);
        usernameField = (EditText) findViewById(R.id.loginUsername);
        passwordField = (EditText) findViewById(R.id.loginPassword);
        
        loginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				username = usernameField.getText().toString();
				password = passwordField.getText().toString();
				
				ParseUser.logInInBackground(username, password, new LogInCallback() {
					
					@Override
					public void done(ParseUser user, ParseException e) {
						if(user != null) {
                            startService(serviceIntent);
                            startActivity(intent);
						} else {
							Toast.makeText(getApplicationContext(), "There was an error logging in.", Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		});
        
        signUpButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				username = usernameField.getText().toString();
				password = passwordField.getText().toString();
				
				ParseUser user = new ParseUser();
				user.setUsername(username);
				user.setPassword(password);
				
				user.signUpInBackground(new SignUpCallback() {
					
					@Override
					public void done(ParseException e) {
							if(e == null) {
	                            startService(serviceIntent);
	                            startActivity(intent);
						} else {
							Toast.makeText(getApplicationContext(), "There was an error signing up.", Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		});
        
    }
    
    @Override
    protected void onDestroy() {
    	stopService(new Intent(this, MessageService.class));
    	super.onDestroy();
    }
}
