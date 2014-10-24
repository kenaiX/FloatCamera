package com.kenai.camera;

import com.kenai.function.camera.XCamera;
import com.kenai.function.setting.XSetting;

import android.content.Context;
import android.content.Intent;

public class KenaiBroadcast extends CameraBroadcast{

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
			if(XSetting.xget_boolean(context, "cemera_auto_face"))
			XCamera.face_picture(context);
		}
		super.onReceive(context, intent);
		
		
	}

}
