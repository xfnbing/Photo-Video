package com.xfnbing.android;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera.Parameters;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

public class VideoPreviewSurfaceView extends AbstractPreviewSurfaceView
		implements SurfaceHolder.Callback {

	public VideoPreviewSurfaceView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public VideoPreviewSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VideoPreviewSurfaceView(Context context) {
		super(context);
	}

	// ////////////////////////////////////////////////////////////

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// mCamera.stopPreview();

		Parameters params = mCamera.getParameters();
		List<String> list = params.getSupportedFocusModes();
		mCamera.cancelAutoFocus();
		if (list.contains(Parameters.FOCUS_MODE_AUTO)) {
			params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		} 
		params.setRotation(90); 
		params.setPreviewFrameRate(24);  
		mCamera.setParameters(params);
		mCamera.setDisplayOrientation(90);

		mCamera.startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i("video get -surfaceDestroyed", "surfaceDestroyed");
		if (mCamera != null) { 
			mCamera.stopPreview(); 
		}
	}
}
