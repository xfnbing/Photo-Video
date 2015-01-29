package com.xfnbing.android;

import java.io.IOException; 
 
 
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

public class CameraPreviewSurfaceView extends AbstractPreviewSurfaceView
		implements SurfaceHolder.Callback { 

	public CameraPreviewSurfaceView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle); 
	}

	public CameraPreviewSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs); 
	}

	public CameraPreviewSurfaceView(Context context) {
		super(context); 
	}

	// @Override
	// protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	//
	// // We purposely disregard child measurements because act as a
	// // wrapper to a SurfaceView that centers the camera preview instead
	// // of stretching it.
	// final int width = resolveSize(getSuggestedMinimumWidth(),
	// widthMeasureSpec);
	// final int height = resolveSize(getSuggestedMinimumHeight(),
	// heightMeasureSpec);
	// setMeasuredDimension(width, height);
	//
	// if (mSupportedPreviewSizes != null) {
	// mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width,
	// height);
	// }
	// }
	//
	// private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
	// final double ASPECT_TOLERANCE = 0.1;
	// double targetRatio = (double) w / h;
	// if (sizes == null)
	// return null;
	//
	// Size optimalSize = null;
	// double minDiff = Double.MAX_VALUE;
	//
	// int targetHeight = h;
	//
	// // Try to find an size match aspect ratio and size
	// for (Size size : sizes) {
	// double ratio = (double) size.width / size.height;
	// if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
	// continue;
	// if (Math.abs(size.height - targetHeight) < minDiff) {
	// optimalSize = size;
	// minDiff = Math.abs(size.height - targetHeight);
	// }
	// }
	//
	// // Cannot find the one match the aspect ratio, ignore the requirement
	// if (optimalSize == null) {
	// minDiff = Double.MAX_VALUE;
	// for (Size size : sizes) {
	// if (Math.abs(size.height - targetHeight) < minDiff) {
	// optimalSize = size;
	// minDiff = Math.abs(size.height - targetHeight);
	// }
	// }
	// }
	// return optimalSize;
	// }

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		System.out.println("surfaceCreated");
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		try {
			if (null != mCamera) {
				mCamera.setPreviewDisplay(holder);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (null != mCamera) {
				mCamera.startPreview();
				// startFaceDetection();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		System.out.println("surfaceChanged");

		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.

		if (null == holder.getSurface()) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			if (null != mCamera) {
				mCamera.stopPreview();
			}
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here

		if (null != mCamera) {
			// mCamera.cancelAutoFocus();
			Camera.Parameters p = mCamera.getParameters();
			mCamera.setDisplayOrientation(90);
			// p.setPictureSize(640, 480);
			p.setJpegQuality(100);
			if (id == 1)
				p.setRotation(270);
			else
				p.setRotation(90); 
			// p.setPreviewSize(1000, 800);
			p.setPictureFormat(PixelFormat.JPEG);
			mCamera.setParameters(p);
		}

		// start preview with new settings
		try {
			if (null != mCamera) {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
				// TODO 自动对焦
				/*
				 * mCamera.autoFocus(new AutoFocusCallback() {
				 * 
				 * @Override public void onAutoFocus(boolean success, Camera
				 * camera) { if (success) { camera.cancelAutoFocus();//
				 * 只有加上了这一句，才会自动对焦。 } }
				 * 
				 * });
				 */
			}

		} catch (Exception e) {
		}
		/*
		 * ivFocus.setImageResource(R.drawable.focus1); LayoutParams
		 * layoutParams = new LayoutParams(LayoutParams.FILL_PARENT,
		 * LayoutParams.FILL_PARENT); ivFocus.setScaleType(ScaleType.CENTER);
		 * addContentView(ivFocus, layoutParams);
		 * ivFocus.setVisibility(VISIBLE);
		 */
	}

	public void startFaceDetection() {

		// Try starting Face Detection

		Camera.Parameters params = mCamera.getParameters();

		// start face detection only *after* preview has started
		if (params.getMaxNumDetectedFaces() > 0) {

			// camera supports face detection, so can start it:

			mCamera.setFaceDetectionListener(new MyFaceDetectionListener());
			mCamera.startFaceDetection();

		}

	}

	class MyFaceDetectionListener implements Camera.FaceDetectionListener {

		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {

			if (faces.length > 0) {
				Log.d("FaceDetection", "face detected: " + faces.length
						+ " Face 1 Location X: " + faces[0].rect.centerX()
						+ "Y: " + faces[0].rect.centerY());
			}

		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		System.out.println("surfaceDestroyed");
		if (null != mCamera) {
			mCamera.stopPreview();
		}

	}

}