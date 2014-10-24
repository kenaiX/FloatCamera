package com.kenai.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import cc.kenai.mainlib.face.XFace;
import cc.kenai.mainlib.face.XFace.MyFace;
import cc.kenai.mainlib.face.XFace.NoResourseException;
import cc.kenai.mainlib.face.XFace.NoresultException;

import com.kenai.function.message.XLog;
import com.kenai.function.message.XToast;
import com.kenai.function.sensor.XSensorListener;
import com.kenai.function.setting.XSetting;
import com.kenai.function.state.XState;
import com.kenai.function.tools.XYingjian;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.Bitmap.Config;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class CameraService {
	private final static String TAG = "CameraService";
	public final static String BROADCAST = "com.kenai.camera.control";
	public final static int ACTION_TAKEPICTUR_FAST = 1;
	public final static int ACTION_TAKEPICTUR_NORMAL = 2;
	public final static int ACTION_TAKEPICTUR_NORMAL_DELAY = 3;
	public final static int ACTION_ADD_VIEW = 4;
	public final static int ACTION_REMOVE_VIEW = 5;
	public final static int ACTION_UPDATEVIEW_CAMERA = 6;
	public final static int ACTION_UPDATEVIEW_SHUTTER = 7;

	final Handler myhandler;
	private final BroadcastReceiver myBroadcast;
	private final XSensorListener myXSensorListener;

	private FocusAndCatchTool mFocusAndCatchTool;
	private ViewTool mViewTool = null;
	private WakeLock xWakeLock = null;
	private int started_volume = 0;
	private boolean state_camera = false;

	private static CameraService myCameraService;
	private static Context context;

	public static Camera mCamera;

	private CameraService(Context _context) {
		context = _context;
		myhandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case ACTION_UPDATEVIEW_SHUTTER:
					mViewTool.updateView(ViewTool.LAYOUPARAMS_CONTROL);
					break;
				case ACTION_UPDATEVIEW_CAMERA:
					mViewTool.updateView(ViewTool.LAYOUPARAMS_MAIN);
					break;
				case ACTION_REMOVE_VIEW:
					remove_view();
					break;

				case ACTION_ADD_VIEW:
					try {
						hand_addview();
					} catch (Exception e) {
						// TODO 自动生成的 catch 块
						XToast.xToast(context, "camera error");
						e.printStackTrace();
					}
					break;
				case ACTION_TAKEPICTUR_FAST:
					if (mFocusAndCatchTool != null) {
						mFocusAndCatchTool.noFocusAndCatch();
					}
					break;

				case ACTION_TAKEPICTUR_NORMAL:
					if (state_camera) {
						mFocusAndCatchTool.focusAndCatch();
					} else {
						start();
						if (xWakeLock == null) {
							PowerManager xPowerManager = (PowerManager) context
									.getSystemService(Context.POWER_SERVICE);
							xWakeLock = xPowerManager
									.newWakeLock(
											PowerManager.ACQUIRE_CAUSES_WAKEUP
													| PowerManager.SCREEN_DIM_WAKE_LOCK,
											"PowerServiceDemo");
							xWakeLock.acquire();
						}
						myhandler.sendEmptyMessageDelayed(
								ACTION_TAKEPICTUR_NORMAL_DELAY, 100);
					}

					break;
				case ACTION_TAKEPICTUR_NORMAL_DELAY:
					if (mFocusAndCatchTool != null) {
						mFocusAndCatchTool.focusAndCatch();
					}

					break;
				}
			}
		};
		myBroadcast = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				if (arg1.getAction().equals(BROADCAST)) {
					int value = arg1.getExtras().getInt("value");
					if (value != 0)
						myhandler.sendEmptyMessage(value);
				} else if (arg1.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
					stop_screen();
				} else if (arg1.getAction().equals(Intent.ACTION_SCREEN_ON)) {
					start_screen();
				} else if (arg1.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
					abortBroadcast();
					Intent intent = new Intent(BROADCAST);
					intent.putExtra("value", ACTION_TAKEPICTUR_NORMAL);
					context.sendBroadcast(intent);
				}

			}

		};
		myXSensorListener = new XSensorListener(Sensor.TYPE_PROXIMITY, context) {
			float value = 100;

			@Override
			public void doInformation(SensorEvent event) {

				if (event.values[0] > value) {
					Intent intent = new Intent(BROADCAST);
					intent.putExtra("value", ACTION_TAKEPICTUR_NORMAL);
					context.sendBroadcast(intent);
				}
				value = event.values[0];
			}
		};

	}

	public final static CameraService getSingleton(Context _context,
			Service _service) {
		// TODO Auto-generated method stub
		if (myCameraService == null) {
			myCameraService = new CameraService(_context);
		}
		return myCameraService;
	}

	private final void stop_screen() {
		if (state_camera)
			stop();
	}

	private final void start_screen() {
		if (!state_camera)
			start();
	}

	public void xCreate() {
		IntentFilter filter = new IntentFilter();
		filter.setPriority(Integer.MAX_VALUE);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_MEDIA_BUTTON);
		filter.addAction(BROADCAST);
		context.registerReceiver(myBroadcast, filter);
		myXSensorListener.mbindSensor();
		start();
	}

	public final void xDestroy() {
		if (xWakeLock != null) {
			xWakeLock.release();
			xWakeLock = null;
		}
		stop();
		context.unregisterReceiver(myBroadcast);
		if (myXSensorListener != null) {
			myXSensorListener.munbindSensor();
		}
		myCameraService = null;
	}

	private final void prepareCamera() throws Exception {
		if (mCamera != null) {
			releaseCamera();
		}
		try {
			mCamera = Camera.open();
			mCamera.setDisplayOrientation(90);
		} catch (Exception e) {
			XLog.xLog_bug(TAG, e.getMessage());
			throw e;
		}
	}

	private final void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}

	}

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

	private final boolean isSpecial() {
		ActivityManager mActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
		String s = rti.get(0).topActivity.getPackageName();
		if (!s.equals("com.android.camera")
				&& !XState.get_isServiceRuning(
						"com.kenai.camera.CameraTake_pictureService", context)
				&& !XState.get_isServiceRuning(
						"com.kenai.camera.CameraFaceService", context)) {
			// XToast.xToast(context, s);
			return false;
		} else
			return true;
	}

	private final synchronized void hand_addview() throws Exception {
		if (!state_camera) {
			prepareCamera();
			mViewTool = new ViewTool(context, myhandler);
			mFocusAndCatchTool = new FocusAndCatchTool();
			mViewTool.showView();
			state_camera = true;
		}
	}

	private final synchronized void remove_view() {
		if (state_camera) {
			mViewTool.removeView();
			mViewTool = null;
			mFocusAndCatchTool = null;
			releaseCamera();
			state_camera = false;
			XLog.xLog("恢复屏幕亮度");
		}
	}

	public final void start() {
		if (!isSpecial())
			myhandler.sendEmptyMessage(ACTION_ADD_VIEW);
	}

	public final void stop() {
		myhandler.sendEmptyMessage(ACTION_REMOVE_VIEW);
	}

	class FocusAndCatchTool {
		private final PictureCallback mPicture;

		public FocusAndCatchTool() {
			mPicture = new PictureCallback() {
				public void onPictureTaken(final byte[] data, Camera camera) {
					if (XSetting.xget_boolean(context, "camera_vibrator")) {
						XYingjian.xvibrator_touch(context);
					}
					mViewTool.getSurfaceView().surfaceChanged(null, 1, 1, 1);
					// surfaceChanged();
					is_in_takepicture = false;
					if (xWakeLock != null) {
						xWakeLock.release();
						xWakeLock = null;
					}
					new Thread (){
						public void run(){
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
								
//								mViewTool.setImage(mViewTool.getFaceView(),PreviewCallBack.rotaingImageView(90, Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), 360, 240, false)));
								
								
								
							} catch (FileNotFoundException e) {
								Log.d("++wangjun", "File not found: " + e.getMessage());
							} catch (Exception e) {
								Log.d("++++wangjun",
										"Error accessing file: " + e.getMessage());
							}
						}
					}.start();

					
				}
			};
		}

		public boolean is_in_takepicture = false;

		private final int FocusState_InFocus = 1, FocusState_FinishFocus = 2,
				FocusState_OutOfFocus = 0;
		private int state = 0;

		public final boolean getHasFocus() {
			if (state == FocusState_OutOfFocus) {
				return false;
			} else {
				return true;
			}
		}

		public final void focus() {
			if (!is_in_takepicture && CameraService.mCamera != null) {
				state = FocusState_InFocus;
				mCamera.autoFocus(new AutoFocusCallback() {
					public void onAutoFocus(boolean success, Camera camera) {
						state = FocusState_FinishFocus;
					}
				});
			}
		}

		public final void focusAndCatch() {
			if (!is_in_takepicture && CameraService.mCamera != null) {
				if (!getHasFocus()) {
					state = FocusState_InFocus;
					mCamera.autoFocus(new AutoFocusCallback() {
						public void onAutoFocus(boolean success, Camera camera) {
							state = FocusState_FinishFocus;
							
							final AudioManager am = (AudioManager) context
									.getSystemService(Context.AUDIO_SERVICE);
							int started_volume = am
									.getStreamVolume(AudioManager.STREAM_SYSTEM);
							am.setStreamVolume(AudioManager.STREAM_SYSTEM, 0,
									AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
							mCamera.takePicture(null, null, mPicture);
							am.setStreamVolume(AudioManager.STREAM_SYSTEM, started_volume,
									AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
							state = FocusState_OutOfFocus;
							is_in_takepicture = true;
						}
					});
				} else {
					if (CameraService.mCamera != null) {
						final AudioManager am = (AudioManager) context
								.getSystemService(Context.AUDIO_SERVICE);
						int started_volume = am
								.getStreamVolume(AudioManager.STREAM_SYSTEM);
						am.setStreamVolume(AudioManager.STREAM_SYSTEM, 0,
								AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
						mCamera.takePicture(null, null, mPicture);
						am.setStreamVolume(AudioManager.STREAM_SYSTEM, started_volume,
								AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
						state = FocusState_OutOfFocus;
						is_in_takepicture = true;
					}
				}
			}
		}

		public final void noFocusAndCatch() {
			if (!is_in_takepicture && CameraService.mCamera != null) {
				final AudioManager am = (AudioManager) context
						.getSystemService(Context.AUDIO_SERVICE);
				int started_volume = am
						.getStreamVolume(AudioManager.STREAM_SYSTEM);
				am.setStreamVolume(AudioManager.STREAM_SYSTEM, 0,
						AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
				mCamera.takePicture(null, null, mPicture);
				am.setStreamVolume(AudioManager.STREAM_SYSTEM, started_volume,
						AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
				state = FocusState_OutOfFocus;
				is_in_takepicture = true;
			}
		}
	}
}

class ViewTool {
	private String TAG = "ViewTool";
	private WindowManager wm;
	private final ControlView mControlView;
	private final MainView mMainView;
	public final static int LAYOUPARAMS_CONTROL = 1, LAYOUPARAMS_MAIN = 2;
	private final Context context;
	private final Handler handler;
	private float[] facePositon = new float[2];
	private int preWidth=0,preHeight=0;
	private WakeLock mWakeLock;

	public ViewTool(final Context context,final Handler handler) {
		this.context = context;
		this.handler = handler;
		wm = (WindowManager) context.getSystemService("window");
		mControlView = new ControlView(context, this);
		mMainView = new MainView(context, this);
		
	}

	public final void startFace() {
		if (CameraService.mCamera != null) {
			if(mWakeLock!=null){
				mWakeLock.release();
				mWakeLock=null;
			}
			PowerManager xPowerManager = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			mWakeLock = xPowerManager
					.newWakeLock(
							PowerManager.ACQUIRE_CAUSES_WAKEUP
									| PowerManager.SCREEN_DIM_WAKE_LOCK,
							"PowerServiceDemo");
			mWakeLock.acquire();
			// CameraService.mCamera.startFaceDetection();
			CameraService.mCamera.setPreviewCallback(new PreviewCallBack() {
				@Override
				public void onPreviewCallback(final Bitmap bmp) {
					new Thread() {
						public void run() {
							try {
								MyFace myFace = XFace.xCatchFace(bmp, true, 1);
								PointF point = new PointF();
								myFace.faces[0].getMidPoint(point);
								float[] f = new float[2];
								float radius = myFace.faces[0].eyesDistance()
										* myFace.scale * 1.2f;
								facePositon[0] = f[0] = point.x * myFace.scale;
								facePositon[1] = f[1] = point.y * myFace.scale;
								final Bitmap mainBM = Bitmap.createBitmap(
										bmp.getWidth(), bmp.getHeight(),
										Config.ARGB_8888);
								bmp.recycle();
								Canvas canvas = new Canvas(mainBM);
								// canvas.drawBitmap(bmp, 0, 0, new Paint());
								XFace.drawRect(canvas, facePositon[0],
										facePositon[1], radius);
								canvas.save();
								handler.post(new Runnable() {
									public void run() {
										if (cameraState == STATE_CATCH_ME) {
											preWidth = mainBM.getWidth();
											preHeight = mainBM.getHeight();
											getFaceView()
													.setImageBitmap(mainBM);
										} 
									}
								});
							} catch (NoresultException e) {
								facePositon[0] = 0;
								facePositon[1] = 0;
								handler.post(new Runnable() {
									public void run() {
										getFaceView().setImageBitmap(null);
									}
								});
							} catch (NoResourseException e) {
								facePositon[0] = 0;
								facePositon[1] = 0;
								e.printStackTrace();
							}
							checkoutWork();
						}
					}.start();

				}

			});
		}
	}

	public final void setImage(final ImageView im,final Bitmap bm){
		handler.post(new Runnable() {
			public void run() {
				im.setImageBitmap(bm);
			}
		});
	}
	public final void stopFace() {
		if (CameraService.mCamera != null) {
			CameraService.mCamera.setPreviewCallback(null);
			
		}
		if (mWakeLock != null) {
			handler.postDelayed(new Runnable() {
				public void run() {
					if (mWakeLock != null) {
						mWakeLock.release();
						mWakeLock = null;
					}

				}
			}, 5000);
		}

	}

	public final static int STATE_NORMAL = 0, STATE_CATCH_ME = 1;

	
	TextToSpeech mSpeech2;
	public  int cameraState=0;
	public final void changeState(int state) {
		if (t != null) {
			t.cancel();
			t = null;
		}
		stopFace();
		switch (state) {
		case STATE_CATCH_ME:
			cameraState=STATE_CATCH_ME;
			startFace();
			t = new Timer();
			t.schedule(new TimerTask() {
private int n=0;
				@Override
				public void run() {
					if (Math.abs(facePositon[0] - preWidth/2) < preWidth/20&&Math.abs(facePositon[1] - preHeight/2) < preHeight/20) {
						if (mSpeech2 == null) {
							mSpeech2 = new TextToSpeech(context,
									new OnInitListener() {

										public void onInit(int status) {
											int result = mSpeech2
													.setLanguage(Locale.ENGLISH);
											if (result == TextToSpeech.LANG_MISSING_DATA
													|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
											} else {
												mSpeech2.speak(
														"ok",
														TextToSpeech.QUEUE_FLUSH,
														null);

											}
										}

									});
						} else {
							mSpeech2.speak("ok", TextToSpeech.QUEUE_FLUSH, null);
						}
						
						Intent intent = new Intent(CameraService.BROADCAST);
						intent.putExtra("value",
								CameraService.ACTION_TAKEPICTUR_NORMAL);
						facePositon[0]=0;
						facePositon[1]=0;
						context.sendBroadcast(intent);
						changeState(STATE_NORMAL);
					}else{
						final String s;
						if (facePositon[0] != 0) {
							if (facePositon[0] - preWidth / 2 > preWidth / 20) {
								s = "left";
							} else if (facePositon[0] - preWidth / 2 < -preWidth / 20) {
								s = "right";
							} else if (facePositon[1] - preHeight / 2 < -preHeight / 20) {
								s = "up";
							} else if (facePositon[1] - preHeight / 2 > preHeight / 20) {
								s = "down";
							} else {
								s = "error";
							}
						} else {
							if(n%3==0)
							s = "no";
							else
								s=null;
							n++;
						}
						if(s!=null)
						if (mSpeech2 == null) {
							mSpeech2 = new TextToSpeech(context,
									new OnInitListener() {

										public void onInit(int status) {
											int result = mSpeech2
													.setLanguage(Locale.ENGLISH);
											if (result == TextToSpeech.LANG_MISSING_DATA
													|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
											} else {
												mSpeech2.speak(
														s,
														TextToSpeech.QUEUE_FLUSH,
														null);

											}
										}

									});
						} else {
							mSpeech2.speak(s, TextToSpeech.QUEUE_FLUSH, null);
						}
					}
				}
			}, 0, 1000);
			break;
default:
	cameraState=STATE_NORMAL;
	break;
		}
	}

	private Timer t;

	public final void showView() {
		if (!mMainView.frameView.isShown()) {
			mMainView.showView(wm);
		} else {
			XLog.xLog_bug(TAG, "showView::mMainView has shown");
		}
		if (!mControlView.controlView.isShown()) {
			mControlView.showView(wm);
		} else {
			XLog.xLog_bug(TAG, "showView::mControlView has shown");
		}
	}

	public final void removeView() {
		changeState(STATE_NORMAL);
		if (mControlView.controlView.isShown()) {
			mControlView.removeView(wm);
		} else {
			XLog.xLog_bug(TAG, "removeView::mControlView hasn't shown");
		}
		if (mMainView.frameView.isShown()) {
			mMainView.removeView(wm);
		} else {
			XLog.xLog_bug(TAG, "removeView::mMainView hasn't shown");
		}
	}

	public final void updateView(int n) {
		switch (n) {
		case LAYOUPARAMS_CONTROL:
			if (mControlView.controlView.isShown()) {
				mControlView.updateView(wm);
			} else {
				XLog.xLog_bug(TAG, "updateView::mControlView hasn't shown");
			}
			break;
		case LAYOUPARAMS_MAIN:
			if (mMainView.frameView.isShown()) {
				mMainView.updateView(wm);
			} else {
				XLog.xLog_bug(TAG, "updateView::mMainView hasn't shown");
			}
			break;
		}

	}

	public final LayoutParams getParams(int n) {
		switch (n) {
		case LAYOUPARAMS_CONTROL:
			return mControlView.getParams();
		case LAYOUPARAMS_MAIN:
			return mMainView.getParams();
		default:
			return null;
		}
	}

	public final CameraPreview getSurfaceView() {
		return mMainView.getSurfaceView();
	}

	public final ImageView getFaceView() {
		return mMainView.getFaceView();
	}

	public final void chageMainViewSize(int change) {
		mMainView.setSize(mMainView.getParams().width + change);
	}

	public final void saveSettings(String name, String value) {
		XSetting.xset_string_int(context, name, value);
	}

	private class ControlView {
		private final static int TouchState_Normal = 0,
				TouchState_Longpress = 1;
		private final String SET_CONTROL_X="cameracontrol_x",SET_CONTROL_Y="cameracontrol_y";
		private final View controlView;
		private final WindowManager.LayoutParams params;
		private final ViewTool mViewTool;
		private final ImageView im_1_1, im_1_2, im_1_3, im_2_1, im_2_2, im_2_3;
		
		abstract class ChildTouchListener implements OnTouchListener{
			private boolean state=false;
			private GestureDetector myGesture=new GestureDetector(context, new GestureDetector.OnGestureListener() {
				
				public boolean onSingleTapUp(MotionEvent e) {
					onClick();
					return false;
				}
				
				public void onShowPress(MotionEvent e) {
					// TODO 自动生成的方法存根
					
				}
				
				public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
						float distanceY) {
					// TODO 自动生成的方法存根
					return false;
				}
				
				public void onLongPress(MotionEvent e) {
					state=true;
					XYingjian.xvibrator(context, 123);
				}
				
				public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
						float velocityY) {
					// TODO 自动生成的方法存根
					return false;
				}
				
				public boolean onDown(MotionEvent e) {
					// TODO 自动生成的方法存根
					return false;
				}
				
			});
			public abstract void onClick();
			public boolean onTouch(View v, MotionEvent event) {
				
				
				if (!state) {
					myGesture.onTouchEvent(event);
					return true;
				} else {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						state = false;
					}
					if(mTouchStartX==0&&mTouchStartY==0)
					{
						mTouchStartX = event.getX();
						mTouchStartY = event.getY();
					}	
					x = event.getRawX();
					y = event.getRawY();
					
					switch (event.getActionMasked()) {
					case MotionEvent.ACTION_MOVE:
						if (event.getEventTime() - event.getDownTime() > 1000) {
							updateViewPosition(event);
						}
						break;
					case MotionEvent.ACTION_UP:
						if (event.getEventTime() - event.getDownTime() < 500) {
							XLog.xLog("" + event.getX());
							if (event.getX() < 100) {
								Intent intent = new Intent(
										CameraService.BROADCAST);
								intent.putExtra("value",
										CameraService.ACTION_TAKEPICTUR_NORMAL);
								context.sendBroadcast(intent);
							} else {
								Intent intent = new Intent(
										CameraService.BROADCAST);
								intent.putExtra("value",
										CameraService.ACTION_TAKEPICTUR_FAST);
								context.sendBroadcast(intent);
							}

						}
						mTouchStartX = mTouchStartY = 0;
						break;
					case MotionEvent.ACTION_POINTER_DOWN:
						if (event.getActionIndex() == 1) {
							XLog.xLog("ACTION_POINTER_DOWN");
							index1_start_X = event.getX(1);
						}
						XLog.xLog("duozhi");
						break;
					case MotionEvent.ACTION_POINTER_UP:

						if (event.getActionIndex() == 1) {
							XLog.xLog("ACTION_POINTER_UP");
							index1_stop_X = event.getX(1);
							mViewTool
									.chageMainViewSize((int) (index1_stop_X - index1_start_X));

						}
						break;
					}
					return true;
				}
			}
			
			
			
			
			private float mTouchStartX;
			private float mTouchStartY;
			private float x;
			private float y;
			float index1_start_X, index1_stop_X;

			private void updateViewPosition(MotionEvent event) {
				int xx = (int) (x - mTouchStartX);
				int yy = (int) (y - mTouchStartY);
				XSetting.xset_string_int(context,SET_CONTROL_X, "" + xx);
				XSetting.xset_string_int(context, SET_CONTROL_Y, "" + yy);
				params.x = xx;
				params.y = yy;
			    mViewTool.updateView(LAYOUPARAMS_CONTROL);
				

			}
			
			
			
		}


		
		public ControlView(final Context context, final ViewTool mViewTool) {
			this.mViewTool = mViewTool;

			this.controlView = (View) View.inflate(context,
					R.layout.camera_control_float, null);
			im_1_1 = (ImageView) controlView.findViewById(R.id.bt_1_1);
			im_1_2 = (ImageView) controlView.findViewById(R.id.bt_1_2);
			im_1_3 = (ImageView) controlView.findViewById(R.id.bt_1_3);
			im_2_1 = (ImageView) controlView.findViewById(R.id.bt_2_1);
			im_2_2 = (ImageView) controlView.findViewById(R.id.bt_2_2);
			im_2_3 = (ImageView) controlView.findViewById(R.id.bt_2_3);
			
			im_1_1.setOnTouchListener(new ChildTouchListener(){

				@Override
				public void onClick() {
					Intent intent = new Intent(CameraService.BROADCAST);
					intent.putExtra("value", CameraService.ACTION_TAKEPICTUR_NORMAL);
					context.sendBroadcast(intent);
				}
				
			});
			im_1_2.setOnTouchListener(new ChildTouchListener(){

				@Override
				public void onClick() {
					Intent intent = new Intent(CameraService.BROADCAST);
					intent.putExtra("value", CameraService.ACTION_TAKEPICTUR_FAST);
					context.sendBroadcast(intent);
				}
				
			});
			im_1_3.setOnTouchListener(new ChildTouchListener(){

				@Override
				public void onClick() {
					mViewTool.chageMainViewSize(50);
				}
				
			});

			im_2_1.setOnTouchListener(new ChildTouchListener(){

				@Override
				public void onClick() {
					if(cameraState==STATE_NORMAL){
					   changeState(STATE_CATCH_ME);
					}else if(cameraState==STATE_CATCH_ME){
						changeState(STATE_NORMAL);
					}else{
						changeState(STATE_NORMAL);
						changeState(STATE_CATCH_ME);
					}
				}
				
			});
			im_2_2.setOnTouchListener(new ChildTouchListener(){

				@Override
				public void onClick() {
//					XToast.xToast(context, "5");
				}
				
			});
			im_2_3.setOnTouchListener(new ChildTouchListener(){

				@Override
				public void onClick() {
					mViewTool.chageMainViewSize(-50);
				}
				
			});
			
			

			this.params = new WindowManager.LayoutParams();
			params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
			params.format = PixelFormat.RGBA_8888;
			params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			params.gravity = Gravity.LEFT | Gravity.TOP;
			params.width = 300;
			params.height = 200;
			params.x = XSetting.xget_int(context, SET_CONTROL_X);
			params.y = XSetting.xget_int(context, SET_CONTROL_Y);
		}

		private final void showView(WindowManager wm) {
			wm.addView(controlView, params);
		}

		private final void removeView(WindowManager wm) {
			wm.removeView(controlView);
		}

		private final void updateView(WindowManager wm) {
			wm.updateViewLayout(controlView, params);
		}

		private final LayoutParams getParams() {
			return params;
		}

	}

	private class MainView {
		private final static String SET_SIZE = "camerafloat_size";
		private final static String SET_X = "camerafloat_x";
		private final static String SET_Y = "camerafloat_y";
		private final FrameLayout frameView;
		private final RelativeLayout mainView;
		private final WindowManager.LayoutParams params;
		private final CameraPreview im_pre;
		private final ImageView im_face;
		private final ViewTool mViewTool;

		public MainView(Context context, ViewTool mViewTool) {
			this.mViewTool = mViewTool;
			this.params = new WindowManager.LayoutParams();
			this.frameView = (FrameLayout) FrameLayout.inflate(context,
					R.layout.camera_float_ver, null);
			load(context);
			this.mainView = (RelativeLayout) this.frameView
					.findViewById(R.id.mainView);
			this.im_pre = new CameraPreview(context, CameraService.mCamera);
			this.im_face = new ImageView(context);
			// this.im_face.setImageResource(R.drawable.lisala);
			final ViewGroup.LayoutParams lp = mainView.getLayoutParams();
			lp.height = lp.width * 3 / 2;
			this.mainView.addView(this.im_pre, lp);
			this.mainView.addView(this.im_face, lp);
		}

		private final void load(Context context) {
			this.params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
			this.params.format = PixelFormat.RGBA_8888;
			this.params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			// this.params.flags=this.params.flags|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
			this.params.gravity = Gravity.LEFT | Gravity.TOP;
			int size = XSetting.xget_int_super(context, SET_SIZE, "" + 300);
			if (size < 1)
				size = 1;
			else if (size > 640)
				size = 640;
			this.params.width = size;
			this.params.height = size * 3 / 2;
			this.params.x = XSetting.xget_int_super(context, SET_X, "100");
			this.params.y = XSetting.xget_int_super(context, SET_Y, "100");

			this.frameView.setOnTouchListener(new OnTouchListener() {
				private float startX_0, startX_1;
				private float moveX_0, moveX_1;
				private int size;
				private float scale_last;

				public boolean onTouch(View v, MotionEvent event) {
					x = event.getRawX();
					y = event.getRawY();
					switch (event.getActionMasked()) {
					case MotionEvent.ACTION_MOVE:
						if (event.getPointerCount() > 1) {
							moveX_0 = event.getX(0);
							moveX_1 = event.getX(1);
							final float scale = Math.abs(moveX_0 - moveX_1)
									/ Math.abs(startX_0 - startX_1);
							XLog.xLog("scale:" + scale);
							if (scale != scale_last) {
								setSize((int) (size * scale));
								scale_last = scale;
							}
						} else if (event.getPointerCount() == 1) {
							updateViewPosition(event);
						}
						break;
					case MotionEvent.ACTION_DOWN: {
						mTouchStartX = event.getX();
						mTouchStartY = event.getY();
					}
						startX_0 = event.getX(0);
						size = params.width;
						break;
					case MotionEvent.ACTION_UP: {
						mTouchStartX = mTouchStartY = 0;
					}
					mViewTool.setImage(mViewTool.getFaceView(),null);
						break;
					case MotionEvent.ACTION_POINTER_DOWN:
						startX_1 = event.getX(1);
						break;
					case MotionEvent.ACTION_POINTER_UP:

						break;
					default:
						break;
					}
					return false;
				}

				private float mTouchStartX;
				private float mTouchStartY;
				private float x;
				private float y;

				private void updateViewPosition(MotionEvent event) {
					int xx = (int) (x - mTouchStartX);
					int yy = (int) (y - mTouchStartY);
					setPosition(xx, yy);

				}
			});
		}

		private final void setPosition(int x, int y) {
			params.x = x;
			params.y = y;
			mViewTool.saveSettings(SET_X, String.valueOf(x));
			mViewTool.saveSettings(SET_Y, String.valueOf(y));
			mViewTool.updateView(LAYOUPARAMS_MAIN);
		}

		private final void setSize(int size) {
			if (size < 1) {
				size = 1;
			} else if (size > 640) {
				size = 640;
			}
			params.width = size;
			params.height = params.width * 3 / 2;
			mViewTool.saveSettings(SET_SIZE, String.valueOf(size));
			mViewTool.updateView(LAYOUPARAMS_MAIN);
		}

		private final void showView(WindowManager wm) {
			wm.addView(frameView, params);
		}

		private final void removeView(WindowManager wm) {
			wm.removeView(frameView);
		}

		private final void updateView(WindowManager wm) {
			wm.updateViewLayout(frameView, params);
		}

		private final LayoutParams getParams() {
			return params;
		}

		private final CameraPreview getSurfaceView() {
			return im_pre;
		}

		private final ImageView getFaceView() {
			return im_face;
		}
	}

}

abstract class PreviewCallBack implements Camera.PreviewCallback {
	public int state = 0;

	public abstract void onPreviewCallback(Bitmap bmp);

	private void checkinWork() {
		this.state++;
	}

	public void checkoutWork() {
		this.state--;
	}

	public void onPreviewFrame(byte[] data, Camera camera) {
		if (state < 2) {
			XLog.xLog("PreviewCallback", "onPreviewFrame");
			checkinWork();
			decodeToBitMap(data, camera);
		}
	}

	public void decodeToBitMap(byte[] data, Camera _camera) {
		Size size = _camera.getParameters().getPreviewSize();
		try {
			YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width,
					size.height, null);
			Log.w("wwwwwwwww", size.width + " " + size.height);
			if (image != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				image.compressToJpeg(new Rect(0, 0, size.width, size.height),
						80, stream);
				Bitmap bmp = BitmapFactory.decodeByteArray(
						stream.toByteArray(), 0, stream.size());
				onPreviewCallback(rotaingImageView(90, bmp));
				stream.close();
			}
		} catch (Exception ex) {
			Log.e("Sys", "Error:" + ex.getMessage());
		}
	}

	/**
	 * rota image
	 */
	public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		Matrix matrix = new Matrix();
		;
		matrix.postRotate(angle);
		Bitmap resizedBitmap = null;
		resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}
}

// @Override
// public void onPreviewCallback(Bitmap bmp) {
// try {
// MyFace myFace = XFace.xCatchFace(bmp, true, 1);
// PointF point = new PointF();
// myFace.faces[0].getMidPoint(point);
// float[] f = new float[2];
// float radius = myFace.faces[0].eyesDistance()
// * myFace.scale * 1.2f;
// float cx = f[0] = point.x * myFace.scale;
// float cy = f[1] = point.y * myFace.scale;
// Bitmap mainBM = Bitmap.createBitmap(bmp.getWidth(),
// bmp.getHeight(), Config.ARGB_8888);
// Canvas canvas = new Canvas(mainBM);
// // canvas.drawBitmap(bmp, 0, 0, new Paint());
// XFace.drawRect(canvas, cx, cy, radius);
// canvas.save();
// im_face.setImageBitmap(mainBM);
// } catch (NoresultException e) {
//
// e.printStackTrace();
// } catch (NoResourseException e) {
// // TODO 自动生成的 catch 块
// e.printStackTrace();
// }
// }

// public void xxx(File file) {
// BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
// bitmapOptions.inSampleSize = 8;
// /**
// * 获取图片的旋转角度，有些系统把拍照的图片旋转了，有的没有旋转
// */
// int degree = ImageDispose.readPictureDegree(file.getAbsolutePath());
//
// Bitmap cameraBitmap = BitmapFactory.decodeFile(file.getPath(),
// bitmapOptions);
// // bitmap = cameraBitmap;
// /**
// * 把图片旋转为正的方向
// */
// // Bitmap bitmap = ImageDispose.rotaingImageView(degree, bitmap);
// // upload(bitmap);
//
// }
//
// static class ImageDispose {
// /**
// * 旋转图片
// *
// * @param angle
// * @param bitmap
// * @return Bitmap
// */
// public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
// // 旋转图片 动作
// Matrix matrix = new Matrix();
// ;
// matrix.postRotate(angle);
// System.out.println("angle2=" + angle);
// // 创建新的图片
//
// Bitmap resizedBitmap = null;
//
// // final int CWJ_HEAP_SIZE = 6* 1024* 1024 ;
// // 设置最小heap内存为6MB大小
// // VMRuntime.getRuntime().setMinimumHeapSize(CWJ_HEAP_SIZE);
//
// try {
// resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
// bitmap.getWidth(), bitmap.getHeight(), matrix, true);
// } catch (Exception e) {
// // TODO 自动生成的 catch 块
// bitmap.recycle();
// }
//
// return resizedBitmap;
// }
//
// /**
// * 读取图片属性：旋转的角度
// *
// * @param path
// * 图片绝对路径
// * @return degree旋转的角度
// */
// public static int readPictureDegree(String path) {
// int degree = 0;
// try {
// ExifInterface exifInterface = new ExifInterface(path);
// int orientation = exifInterface.getAttributeInt(
// ExifInterface.TAG_ORIENTATION,
// ExifInterface.ORIENTATION_NORMAL);
// switch (orientation) {
// case ExifInterface.ORIENTATION_ROTATE_90:
// degree = 90;
// break;
// case ExifInterface.ORIENTATION_ROTATE_180:
// degree = 180;
// break;
// case ExifInterface.ORIENTATION_ROTATE_270:
// degree = 270;
// break;
// }
// } catch (IOException e) {
// e.printStackTrace();
// }
// return degree;
// }
//
// static int computeSampleSize(BitmapFactory.Options options, int target) {
// int w = options.outWidth;
// int h = options.outHeight;
// int candidateW = w / target;
// int candidateH = h / target;
// int candidate = Math.max(candidateW, candidateH);
// if (candidate == 0)
// return 1;
// if (candidate > 1) {
// if ((w > target) && (w / candidate) < target)
// candidate -= 1;
// }
// if (candidate > 1) {
// if ((h > target) && (h / candidate) < target)
// candidate -= 1;
// }
// return candidate;
// }
//
// }
