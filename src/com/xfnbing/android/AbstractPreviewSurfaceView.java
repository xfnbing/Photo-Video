package com.xfnbing.android;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class AbstractPreviewSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	protected Camera mCamera;
	protected int id = 0; 
	public AbstractPreviewSurfaceView(Context context) {
		super(context);
		this.init();
	}

	public AbstractPreviewSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init();
	}

	protected void init() {
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		getHolder().addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public AbstractPreviewSurfaceView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.init();
	}

	public void setCamera(Camera camera) {
		mCamera = camera;
	}
	public void setCameraId(int id) {
		this.id = id;
	}

}