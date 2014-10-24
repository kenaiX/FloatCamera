package com.kenai.camera;

import com.kenai.function.message.XLog;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
	private final String TAG = "CameraPreview";
	private final SurfaceHolder myHolder;
	private final Camera myCamera;

	public CameraPreview(Context context, Camera camera) {
		super(context);
		myCamera = camera;
		myHolder = getHolder();
		myHolder.addCallback(this);
		// necessary before android3.0
		myHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		XLog.xLog(TAG, "surfaceChanged");
		/*
		 * 如果预览无法更改或旋转，注意此处的事件 确保在缩放或重排时停止预览
		 */
		if (myHolder.getSurface() == null) {
			// 预览surface不存在
			return;
		}
		/*
		 * 在此进行缩放、旋转和重新组织格式 以新的设置启动预览 此时就需要用到自己的myHolder了
		 */
		try {
			myCamera.setPreviewDisplay(myHolder);// 通过holder来设置预览的格式
			myCamera.startPreview();// 开启预览，少了这句话就不能启动预览了。
		} catch (Exception e) {
			XLog.xLog(TAG, "Error setting camera preview:\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		XLog.xLog(TAG, "surfaceCreated");
		// surface已被创建，现在把预览画面的位置通知摄像头
		try {
			Camera.Parameters parameters = myCamera.getParameters();
			// 设置预览照片的大小
			parameters.setPreviewSize(720, 480);
			parameters.setPreviewFormat(ImageFormat.NV21);
			parameters.setFocusMode(Camera.Parameters.SCENE_MODE_AUTO);
			parameters.setPictureFormat(ImageFormat.JPEG);
			parameters.setJpegQuality(80);
			// 设置照片的大小
			parameters.setPictureSize(parameters.getPictureSize().width,
					parameters.getPictureSize().height);
			// parameters.setRotation(270);
			// android2.3.3以后无需下步
			myCamera.setParameters(parameters);
		} catch (Exception e) {
		}
		try {
			myCamera.setPreviewDisplay(holder);
			myCamera.startPreview();
		} catch (Exception e) {
			XLog.xLog(TAG, "Error setting camera preview:\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		XLog.xLog(TAG, "surfaceDestroyed");
	}
}
