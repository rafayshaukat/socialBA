package me.rafayshaukat.socialba;

import com.parse.Parse;
import com.parse.ParseObject;

import android.app.Application;

public class MyApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "v4ezung2qGy2p52zNu4dk6eREIBReD51uuxRKROf", "E720leUyfBkyJQSFIC4tKfmyj7o6xBcpyiySUQ5U");
	}
	
}
