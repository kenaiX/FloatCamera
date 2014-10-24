package com.kenai.camera;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CameraBroadcast extends BroadcastReceiver {
	Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		if(intent.getAction().equals("com.kenai.camera.face")){
			context.startService(new Intent(context,CameraFaceService.class));
			abortBroadcast();
		}
		if (intent.getAction().equals("com.kenai.camera.take_picture")) {
			context.startService(new Intent(context,
					CameraTake_pictureService.class));
			abortBroadcast();
		}
	}

	
}
