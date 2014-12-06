package me.rafayshaukat.socialba;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.parse.ParseUser;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.WritableMessage;

public class MessageService extends Service implements SinchClientListener {
	
	private static final String APP_KEY = "1d362f42-18bf-4165-82ce-52a27ec92c77";
	private static final String APP_SECRET = "UR/cSsmPf0OokitiRMUVbw==";
	private static final String ENVIRONMENT = "sandbox.sinch.com";
	private final MessageServiceInterface serviceInterface = new MessageServiceInterface();
	
	private SinchClient sinchClient = null;
	private MessageClient messageClient = null;
	private String currentUserId;
	
	private LocalBroadcastManager broadcaster;
	private Intent broadcastIntent = new Intent("me.rafayshaukat.socialba.ListUserActivity");
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		//get the current user ID from Parse
		currentUserId = ParseUser.getCurrentUser().getObjectId();
		if(currentUserId != null && !isSinchClientStarted()) {
			startSinchClient(currentUserId);
		}
		
		broadcaster = LocalBroadcastManager.getInstance(this);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void startSinchClient(String username) {
		sinchClient = Sinch.getSinchClientBuilder()
				.context(this)
				.userId(username)
				.applicationKey(APP_KEY)
				.applicationSecret(APP_SECRET)
				.environmentHost(ENVIRONMENT)
				.build();
		
		//This Client Listener requires that you define a few methods below.
		sinchClient.addSinchClientListener(this);
		
		sinchClient.setSupportMessaging(true);
		sinchClient.setSupportActiveConnectionInBackground(true);
		
		sinchClient.checkManifest();
		sinchClient.start();
	}
	
	private boolean isSinchClientStarted() {
		return sinchClient != null && sinchClient.isStarted();
	}
	
	//The next 5 methods are for the sinch client listener
	@Override
	public void onClientFailed(SinchClient client, SinchError error) {
        broadcastIntent.putExtra("success", false);
        broadcaster.sendBroadcast(broadcastIntent);
		sinchClient = null;
	}

	@Override
	public void onClientStarted(SinchClient client) {
        broadcastIntent.putExtra("success", true);
        broadcaster.sendBroadcast(broadcastIntent);

		client.startListeningOnActiveConnection();
		messageClient = client.getMessageClient();
	}

	@Override
	public void onClientStopped(SinchClient client) {
		sinchClient = null;
	}

	@Override
	public void onLogMessage(int arg0, String arg1, String arg2) {}

	@Override
	public void onRegistrationCredentialsRequired(SinchClient arg0, ClientRegistration arg1) {}
	
	/*---------------------------------------------------*/

	@Override
	public IBinder onBind(Intent intent) {
		return serviceInterface;
	}
	
	public void sendMessage(String recipientUserId, String textBody) {
		if(messageClient != null) {
			WritableMessage message = new WritableMessage(recipientUserId, textBody);
			messageClient.send(message);
		}
	}
	
	public void addMessageClientListener(MessageClientListener listener) {
		if(messageClient != null) {
			messageClient.addMessageClientListener(listener);
		}
	}
	
	public void removeMessageClientListener(MessageClientListener listener) {
		if(messageClient != null) {
			messageClient.removeMessageClientListener(listener);
		}
	}
	
	@Override
	public void onDestroy() {
		sinchClient.stopListeningOnActiveConnection();
		sinchClient.terminate();
	}
	
	//Public Interface for ListUsersActivity and MessagingActivity
	public class MessageServiceInterface extends Binder {
		
		public void sendMessage(String recipientUserId, String textBody) {
			MessageService.this.sendMessage(recipientUserId, textBody);
		}
		
		public void addMessageClientListener(MessageClientListener listener) {
			 MessageService.this.addMessageClientListener(listener);
		}
		
		public void removeMessageClientListener(MessageClientListener listener) {
			MessageService.this.removeMessageClientListener(listener);
		}
		
		 public boolean isSinchClientStarted() {
			 return MessageService.this.isSinchClientStarted();
		 }
	}

}
