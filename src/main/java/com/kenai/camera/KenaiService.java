package com.kenai.camera;


import com.kenai.function.message.XToast;
import com.kenai.function.mzstore.XMZyanzheng;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KenaiService extends Service{
	CameraService myCameraService;
	@Override
	public void onCreate() {
		super.onCreate();
		myCameraService=CameraService.getSingleton(getBaseContext(), null);
		myCameraService.xCreate();
		if(!XMZyanzheng.get_isLawful(getBaseContext())){
			XToast.xToast(getBaseContext(), "证书无效，请购买或刷新！");
			stopSelf();
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		myCameraService.xDestroy();
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
