package com.kenai.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.kenai.function.message.XLog;
import com.kenai.function.message.XToast;
import com.kenai.function.mzstore.XMZyanzheng;
import com.kenai.function.setting.XSetting;
import com.kenai.function.state.XState;
import com.kenai.function.tools.XYingjian;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;

public class CameraFaceService extends Service {
	static Context context;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private final boolean isSpecial() {
		ActivityManager mActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
		String s = rti.get(0).topActivity.getPackageName();
		if (!s.equals("com.android.camera")) {
			// XToast.xToast(context, s);
			return false;
		} else
			return true;
	}

	int started_volume;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getBaseContext();
		final AudioManager am = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		started_volume = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
		am.setStreamVolume(AudioManager.STREAM_SYSTEM, 0,
				AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		if (XMZyanzheng.get_isLawful(context)) {

			if (Camera.getNumberOfCameras() >= 1) {
				if (!XState.get_isServiceRuning(
						"com.kenai.camera.KenaiService", context)
						&& !XState.get_isServiceRuning(
								"com.kenai.camera.CameraTake_pictureService",
								context)) {
					if (!isSpecial()) {
						// if(Math.random()>(double)0.5)
						// start(0);
						// else
						start(1);
						myhandler.sendEmptyMessageDelayed(1, 500);
					} else {
						stopSelf();
						XToast.xToast(context, "存在冲突程序");
					}

				} else {
					XToast.xToast(context, "存在冲突程序");
				}
			}

			myhandler.sendEmptyMessageDelayed(0, 5000);
		} else {
			stopSelf();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		final AudioManager am = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		am.setStreamVolume(AudioManager.STREAM_SYSTEM, started_volume,
				AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		stop();
	}

	Handler myhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				mCamera.autoFocus(new AutoFocusCallback() {
					public void onAutoFocus(boolean success, Camera camera) {
						mCamera.takePicture(null, null, mPicture);
					}
				});
				break;
			default:
				stopSelf();
				break;
			}

		}
	};

	public static boolean state_camera;
	private Camera mCamera;
	private CameraPreview cameraPreview;
	private WindowManager.LayoutParams wmParams_camera;

	private void prepareCamera(int i) {
		if (mCamera != null) {
			releaseCamera();
		}
		try {

			mCamera = Camera.open(i);

		} catch (Exception e) {
			mCamera = null;
			stopSelf();
		}
		load_view();
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}

	}

	private void load_view() {
		setPhotoView();
	}

	private void setPhotoView() {
		cameraPreview = new CameraPreview(context, mCamera) {

			// @Override
			public void onPreviewCallback(Bitmap bmp) {
				// TODO 自动生成的方法存根

			}

		};
		cameraPreview.setOnTouchListener(new OnTouchListener() {
			private float mTouchStartX;
			private float mTouchStartY;
			private float x;
			private float y;

			public boolean onTouch(View v, MotionEvent event) {
				x = event.getRawX();
				y = event.getRawY();
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mTouchStartX = event.getX();
					mTouchStartY = event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					updateViewPosition();
					break;
				case MotionEvent.ACTION_UP:
					updateViewPosition();
					mTouchStartX = mTouchStartY = 0;
					break;
				}

				return true;

			}

			private void updateViewPosition() {
				int xx = (int) (x - mTouchStartX);
				int yy = (int) (y - mTouchStartY);
				XSetting.xset_string_int(context, "xx", "" + xx);
				XSetting.xset_string_int(context, "yy", "" + yy);
				wmParams_camera.x = xx;
				wmParams_camera.y = yy;
				WindowManager wm = (WindowManager) context
						.getSystemService("window");
				wm.updateViewLayout(cameraPreview, wmParams_camera); // 刷新显示
			}

		});
	}

	private PictureCallback mPicture = new PictureCallback() {
		public void onPictureTaken(final byte[] data, Camera camera) {
			if (XSetting.xget_boolean(context, "camera_vibrator")) {
				XYingjian.xvibrator_touch(context);
			}
			new Thread() {
				public void run() {
					File pictureFile = getOutputMediaFile();
					if (pictureFile == null) {
						Log.d("++ photo",
								"Error creating media file, check storage permissions: ");
						return;
					}
					try {
						FileOutputStream fos = new FileOutputStream(pictureFile);
						fos.write(data);
						fos.close();
					} catch (FileNotFoundException e) {
						Log.d("++wangjun", "File not found: " + e.getMessage());
					} catch (Exception e) {
						Log.d("++++wangjun",
								"Error accessing file: " + e.getMessage());
					}
				}
			}.start();
			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			stop();
			stopSelf();

		}
	};

	private final static File getOutputMediaFile() {
		if (Environment.getExternalStorageState() == Environment.MEDIA_REMOVED) {
			XToast.xToast(context, "内存卡拔出");
		}

		File mediaStorageDir = CameraEnv.getOutputFile();
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("KenaiCamera", "failed to create directory");
				return null;
			} else {
			}
		}
		// 创建媒体文件名
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile; // 在静态方法里得用静态的变量，我把这个放在class开头，但是需要添加static

		mediaFile = new File(mediaStorageDir.getPath() + File.separator +

		"IMG_" + timeStamp + ".jpg");

		return mediaFile;

	}

	public void start(int i) {
		if (state_camera == false) {
			prepareCamera(i);
			WindowManager wm = (WindowManager) context.getApplicationContext()
					.getSystemService("window");
			wmParams_camera = new WindowManager.LayoutParams();
			wmParams_camera.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			wmParams_camera.format = PixelFormat.RGBA_8888;
			wmParams_camera.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			wmParams_camera.gravity = Gravity.LEFT | Gravity.TOP;
			int size = 1;
			wmParams_camera.width = wmParams_camera.height = size;
			wm.addView(cameraPreview, wmParams_camera);

			state_camera = true;
		}
	}

	public void stop() {
		if (state_camera) {
			releaseCamera();
			WindowManager wm = (WindowManager) context.getApplicationContext()
					.getSystemService("window");
			wm.removeView(cameraPreview);
			state_camera = false;
			XLog.xLog("恢复屏幕亮度");
		}
	}
}
