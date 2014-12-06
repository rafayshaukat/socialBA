package me.rafayshaukat.socialba;

import java.util.List;

import com.parse.ParseUser;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MessagingActivity extends Activity {
	
	private String recipientId;
	private EditText messageBodyField;
	private String messageBody;
	private MessageService.MessageServiceInterface messageService;
	private String currentUserId;
	private ServiceConnection serviceConnection = new MyServiceConnection();
	private MessageClientListener messageClientListener = new MyMessageClientListener();
	private ListView messagesList;
	private MessageAdapter messageAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_messaging);
		
		bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);
		
		//Get recipientId from the Intent
		Intent intent = getIntent();
		recipientId = intent.getStringExtra("RECIPIENT_ID");
		currentUserId = ParseUser.getCurrentUser().getObjectId();
		
		messagesList = (ListView) findViewById(R.id.listMessages);
		messageAdapter = new MessageAdapter(this);
		messagesList.setAdapter(messageAdapter);
		
		messageBodyField = (EditText) findViewById(R.id.messageBodyField);
		
		//Listen for a click on the send button
		findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				messageBody = messageBodyField.getText().toString();
				if (messageBody.isEmpty()) {
				    Toast.makeText(getApplicationContext(), "Please enter a message", Toast.LENGTH_LONG).show();
				    return;
				}
				
				messageService.sendMessage(recipientId, messageBody);
		        messageBodyField.setText("");
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		messageService.removeMessageClientListener(messageClientListener);
		unbindService(serviceConnection);
		super.onDestroy();
	}
	
	private class MyServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			messageService = (MessageService.MessageServiceInterface) iBinder;
			messageService.addMessageClientListener(messageClientListener);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			messageService = null;
		}
	}
	
	private class MyMessageClientListener implements MessageClientListener {

		@Override
		public void onIncomingMessage(MessageClient client, Message message) {
			if(message.getSenderId().equals(recipientId)) {
				WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
				messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING);
			}
		}

		@Override
		public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {}

		@Override
		public void onMessageFailed(MessageClient client, Message message, MessageFailureInfo failureInfo) {
			Toast.makeText(MessagingActivity.this, failureInfo.getSinchError().getMessage(), Toast.LENGTH_LONG).show();
		}

		@Override
		public void onMessageSent(MessageClient client, Message message, String recipientId) {
			WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
			messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING);
		}

		@Override
		public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {}
	}
}
