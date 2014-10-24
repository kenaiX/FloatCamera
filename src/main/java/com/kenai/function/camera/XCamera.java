package com.kenai.function.camera;

import com.kenai.camera.CameraTake_pictureService;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class XCamera {
	/**
	 * 检测摄像头是否存在
	 * @param context
	 * @return
	 */
		public static boolean checkCameraHardware(Context context) {
			if (context.getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_CAMERA)) {
				return true; // 摄像头存在
			} else {
				return false;
			}
		}
		
		/**
		 * 发送顺序广播使用后置摄像头拍照
		 * 
		 */
	public static void take_picture(Context context) {
		context.startService(new Intent(context,
				CameraTake_pictureService.class));
	}
		/**
		 * 发送顺序广播使用前置摄像头拍照
		 * 
		 */
		public static void face_picture(Context context) {
			context.sendOrderedBroadcast(new Intent("com.kenai.camera.face"), null);
		}
		
}
