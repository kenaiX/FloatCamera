package com.kenai.camera;

import com.kenai.function.camera.XCamera;
import com.kenai.function.message.XLog;
import com.kenai.function.mzstore.XMZyanzheng;
import com.kenai.function.setting.XSetting;
import com.kenai.function.state.XState;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;

public class CameraTime extends Service {
	private Context context;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		context = getBaseContext();
		if (XMZyanzheng.get_isLawful(context)) {
			start();
		}
	}

	@Override
	public void onDestroy() {
		stop();
		super.onDestroy();
	}

	private WindowManager.LayoutParams wmParams;
	private ImageView controlView;

	private final Handler myhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			XCamera.take_picture(context);
		}
	};

	private final void start() {
		prevent();
		loadFuction();
		loadControlView();

	}

	private final void stop() {
		removeFunction();
	}

	private final void prevent() {
		if (XState
				.get_isServiceRuning("com.kenai.camera.KenaiService", context)) {
			stopService(new Intent(context, KenaiService.class));
		}
	}

	BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			myhandler.sendEmptyMessage(1);

		}

	};

	private final void removeFunction() {
		cancelUpdateBroadcast();
		context.unregisterReceiver(myBroadcastReceiver);
	}

	private final void loadFuction() {
		IntentFilter filter = new IntentFilter(CAMERA_TIME_FUZHU);
		context.registerReceiver(myBroadcastReceiver, filter);
		sendUpdateBroadcast();
	}

	private final void loadControlView() {
		controlView = new ImageView(context);
		controlView.setBackgroundResource(R.drawable.camera_time);
		controlView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				WindowManager wm = (WindowManager) context
						.getApplicationContext().getSystemService("window");
				wm.removeView(controlView);
				stopSelf();
			}
		});
		WindowManager wm = (WindowManager) context.getApplicationContext()
				.getSystemService("window");
		wmParams = new WindowManager.LayoutParams();
		wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		wmParams.format = PixelFormat.RGBA_8888;
		wmParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		wmParams.gravity = Gravity.CENTER | Gravity.TOP;
		wmParams.width = 160;
		wmParams.height = 47;
		wm.addView(controlView, wmParams);
	}

	private final String CAMERA_TIME_FUZHU = "com.kenai.battery.CAMERA_TIME_FUZHU";
	private final int FLAG_CANCEL_CURRENT = 268435456;

	/**
	 * 辅助满电报警
	 * 
	 * @param ctx
	 */
	public void sendUpdateBroadcast() {
		XLog.xLog("send to start update broadcase,delay time :");
		AlarmManager xAlarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(CAMERA_TIME_FUZHU);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, FLAG_CANCEL_CURRENT);

		int period = XSetting.xget_int(context, "cameraTime_period");
		if (period < 1) {
			period = 1;
		}
		xAlarmManager
				.setInexactRepeating(AlarmManager.RTC_WAKEUP,
						System.currentTimeMillis() + 1000, period * 1000,
						pendingIntent);
	}

	/**
	 * 取消定时执行(有如闹钟的取消)
	 * 
	 * @param ctx
	 */
	public void cancelUpdateBroadcast() {
		AlarmManager xAlarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(CAMERA_TIME_FUZHU);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, FLAG_CANCEL_CURRENT);
		xAlarmManager.cancel(pendingIntent);
	}

}
