package com.kenai.camera;


import com.kenai.function.message.XLog;
import com.kenai.function.message.XToast;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Push娑堟伅澶勭悊receiver
 */
public abstract class PushMessageReceiver extends BroadcastReceiver {
//	public static final String TAG = PushMessageReceiver.class.getSimpleName();
//	AlertDialog.Builder builder;
//
//	/**
//	 * 
//	 * 
//	 * @param context
//	 *            Context
//	 * @param intent
//	 *            鎺ユ敹鐨刬ntent
//	 */
//	@Override
//	public void onReceive(final Context context, Intent intent) {
//
//		if (intent.getAction().equals(PushConstants.ACTION_MESSAGE)) {
//			// 处理push消息
//			String message = intent.getExtras().getString(
//					PushConstants.EXTRA_PUSH_MESSAGE_STRING);
//			if (message != null) {
//				XToast.xToast(context, message);
//
//				Intent intent2 = new Intent(CameraService.BROADCAST);
//				intent2.putExtra("value",
//						CameraService.ACTION_TAKEPICTUR_NORMAL);
//				context.sendBroadcast(intent2);
//
//			}
//			// // PushConstants.EXTRA_EXTRA保存服务端推送下来的附加字段。这是个 JSON
//			// // 字符串。//对应管理控制台上的“自定义内容”
//			// String content = intent.getExtras().getString(
//			// PushConstants.EXTRA_EXTRA);
//			// try {
//			// JSONObject json=new JSONObject(content);
//			// } catch (JSONException e) {
//			// // TODO 自动生成的 catch 块
//			// e.printStackTrace();
//			// }
//			// XLog.xLog(content);
//			//
//			// XToast.xToast(context, message);
//
//		} else if (intent.getAction().equals(PushConstants.ACTION_RECEIVE)) {
////			XToast.xToast(context, "bind");
//			// 处理 bind、setTags等方法口的返回数据
//			final String method = intent
//					.getStringExtra(PushConstants.EXTRA_METHOD);
//			final int errorCode = intent
//					.getIntExtra(PushConstants.EXTRA_ERROR_CODE,
//							PushConstants.ERROR_SUCCESS);
//			final String content = new String(
//					intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT));
//			XLog.xLog(TAG+">>>"+"onMessage: method: " + method);
//			XLog.xLog(TAG+">>>"+"onMessage: result : " + errorCode);
//			XLog.xLog(TAG+">>>"+"onMessage: content : " + content);
//			// 根据 method不同进行不同的处理。 errorCode 也需要处理，有可能成功，有可能失败，
//			// 比如 access token过期
//		}
//		// else if (intent.getAction().equals(
//		// PushConstants.ACTION_RECEIVER_NOTIFICATION_CLICK)) {
//		// // 通知标题
//		// String title = intent
//		// .getStringExtra(PushConstants.EXTRA_NOTIFICATION_TITLE);
//		// // 通知内容
//		// String content = intent
//		// .getStringExtra(PushConstants.EXTRA_NOTIFICATION_CONTENT);
//		// // PushConstants.EXTRA_EXTRA保存服务端推送下来的附加字段。这是个 JSON
//		// // 字符串。//对应管理控制台上的“自定义内容”
//		// String conten2 = intent.getExtras().getString(
//		// PushConstants.EXTRA_EXTRA);
//		// }
//	}

}
