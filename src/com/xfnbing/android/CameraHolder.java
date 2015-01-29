package com.xfnbing.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.xfnbing.android.CameraHolder.OutputMediaFileType;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;

public class CameraHolder {

	public static final CameraHolder INSTANCE = new CameraHolder();
	private int mCameraDefaultId;

	private CameraHolder() {
		mCameraDefaultId = getDefaultCameraId();
	}

	private Camera mCamera = null;

	public Camera getCamera() {
		if (null == mCamera) {
			System.out.println("camera getting");
			mCamera = getCameraInstance(mCameraDefaultId);
		}
		return mCamera;
	}

	public Camera requestCamera(AbstractPreviewSurfaceView... mPreview) {

		if (null == mCamera) {
			mCamera = getCameraInstance(mCameraDefaultId);
		}/*
		 * else{ mCamera.stopPreview(); mCamera.release(); mCamera = null;
		 * mCamera = getCameraInstance(cameraPosition); }
		 */
		for (AbstractPreviewSurfaceView each : mPreview) {
			each.setCamera(mCamera);
			each.setCameraId(cameraPosition);
		}
		return mCamera;
	}

	public void releaseCamera(AbstractPreviewSurfaceView... mPreview) {
		if (mCamera != null) {
			for (AbstractPreviewSurfaceView each : mPreview) {
				each.setCamera(null);
			}
			mCamera.release();
			mCamera = null;
			System.out.println("release success");
		}
	}

	public int cameraPosition = 0;

	public void changeCamera() {

		CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(mCameraDefaultId, info);
		int cameraCount = 0;
		CameraInfo cameraInfo = new CameraInfo();
		cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数
		for (int i = 0; i < cameraCount; i++) {
			Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT
					&& info.facing != CameraInfo.CAMERA_FACING_FRONT) {
				mCameraDefaultId = i;
				mCamera = getCameraInstance(mCameraDefaultId);// 打开当前选中的摄像头
				cameraPosition = 0;
				System.out.println("现在摄像头    " + cameraPosition);
				break;
			}

			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK
					&& info.facing != CameraInfo.CAMERA_FACING_BACK) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
				// CAMERA_FACING_BACK后置
				// if ((i + 1) < cameraCount)
				// mCameraDefaultId = i + 1;
				// else if ((i - 1) > cameraCount)
				// mCameraDefaultId = i - 1;
				// else
				mCameraDefaultId = i;
				System.out.println("现在是前置摄像头   mCameraDefaultId  "
						+ mCameraDefaultId);
				mCamera = getCameraInstance(mCameraDefaultId);// 打开当前选中的摄像头
				cameraPosition = 1;
				System.out.println("现在是前置摄像头   " + cameraPosition);
				break;
			}
		}
	}

	public boolean isFront() {
		boolean result = true;
		CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(mCameraDefaultId, info);

		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
			result = true;
		} else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
			result = false;
		}
		return result;
	}

	public void setDefaultCamera() {
		int defaultId = -1;

		// Find the total number of cameras available
		int mNumberOfCameras = Camera.getNumberOfCameras();

		// Find the ID of the default camera
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < mNumberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				defaultId = i;
				break;
			}
		}
		if (-1 == defaultId) {
			if (mNumberOfCameras > 0) {
				defaultId = 0;
			} else {
				throw new RuntimeException("No Available Camera");
			}
		}
		cameraPosition = 1;
		mCameraDefaultId = defaultId;
	}

	private int getDefaultCameraId() {
		int defaultId = -1;
		// Find the total number of cameras available
		int mNumberOfCameras = Camera.getNumberOfCameras();

		// Find the ID of the default camera
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < mNumberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				defaultId = i;
				cameraPosition = 1;
			}
		}
		if (-1 == defaultId) {
			if (mNumberOfCameras > 0) {
				defaultId = 0;
			} else {
				throw new RuntimeException("No Available Camera");
			}
		}
		return defaultId;
	}

	private Camera getCameraInstance(int cameraId) {
		Camera c = null;
		try {
			c = Camera.open(cameraId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	public static enum OutputMediaFileType {
		MEDIA_TYPE_IMAGE, MEDIA_TYPE_VIDEO;
	}

	/** Create a File for saving an image or video */
	public static File getOutputMediaFile(OutputMediaFileType type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = null;
		// This location works best if you want the created images to be
		// shared
		// between applications and persist after your app has been
		// uninstalled.
		String path = Environment.getExternalStorageDirectory().getPath()+"//";
		// 创建文件夹存放视频
		mediaStorageDir = new File(path);
		if (!mediaStorageDir.exists()) {
			mediaStorageDir.mkdir();
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss",
				Locale.getDefault()).format(new Date());
		File mediaFile = null;
		if (OutputMediaFileType.MEDIA_TYPE_IMAGE.equals(type)) {
			path = path + "images";
			// Create the storage directory if it does not exist
			mediaStorageDir = new File(path);
			if (!mediaStorageDir.exists()) {
				mediaStorageDir.mkdir();
			}
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG" + timeStamp + ".jpg");
		} else if (OutputMediaFileType.MEDIA_TYPE_VIDEO.equals(type)) {
			path = path + "video";
			// Create the storage directory if it does not exist
			mediaStorageDir = new File(path);
			if (!mediaStorageDir.exists()) {
				mediaStorageDir.mkdir();
			}
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

}

class PictureCallbackHandler implements PictureCallback {

	private File outputFile = null;
	private Context context;

	public PictureCallbackHandler(Context context) {
		this.outputFile = CameraHolder
				.getOutputMediaFile(OutputMediaFileType.MEDIA_TYPE_IMAGE);
		this.context = context;
	}

	public File getOutputFile() {
		return outputFile;
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		File pictureFile = this.outputFile;
		if (pictureFile == null) {
			return;
		}

		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
			CameraHolder.INSTANCE.getCamera().stopPreview();
			CameraMainActivity cameraActivity = (CameraMainActivity) this.context;
			cameraActivity.toNext();
		} catch (FileNotFoundException e) {
			System.out.println("photo save FileNotFoundException : "+e.getMessage());
		} catch (IOException e) {
			System.out.println("photo save IOException : "+e.getMessage());
		} 

	}
}
