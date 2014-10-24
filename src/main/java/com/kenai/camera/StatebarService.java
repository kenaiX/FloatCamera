package com.kenai.camera;

import com.kenai.function.mzstore.XMZyanzheng;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StatebarService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		// TODO 自动生成的方法存根
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Notification noti=new Notification(R.drawable.ic_launcher, "", 0);
		PendingIntent pi=PendingIntent.getService(this, 0, new Intent(this,CameraTake_pictureService.class), 0);
		noti.setLatestEventInfo(this, "Catch", "click to snap a picture", pi);
		if(XMZyanzheng.get_isLawful(this));
		startForeground(R.drawable.ic_launcher, noti);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopForeground(true);
		
	}

}
