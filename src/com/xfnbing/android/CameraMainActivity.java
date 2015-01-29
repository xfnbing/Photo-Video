package com.xfnbing.android;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.xfnbing.android.CameraHolder.OutputMediaFileType;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class CameraMainActivity extends Activity {

	private AbstractPreviewSurfaceView cameraPreview;
	private AbstractPreviewSurfaceView videoPreview;
	private ImageView imageViewer;
	private VideoView videoViewer;
	private DisplayMetrics metrics;
	private FrameLayout frameLayout;
	private final int CROP_BIG_PICTURE = 11;
	private String targetFilePath = null;
	private Uri uri = Uri
			.parse("file://" + Environment.getExternalStorageDirectory()
					+ "/GO/images/temp.png");
	private int isMovie = 0;
	private RelativeLayout rlTakePhoto, rlTakeVideo;// titleMenu, 
	private Button btnChangeCamera;
	private TextView tvTime;
	private Button btnToCamere;
	private boolean isLightOn = false;
	private Button btnLight;
	private Button cButton;
	private Button vButton;
	private Button btnToVideo;
	private TimerTask task = null;
	private TouchEvent touchListener = new TouchEvent();

	private static enum LastAction {
		TAKE_PHOTO, TAKE_VIDEO, CANCLE;
	}

	private static enum LastCameraAction {
		TAKE_PHOTO, START_PREVIEW;
		public static LastCameraAction nextAction(LastCameraAction lastAction) {
			return START_PREVIEW.equals(lastAction) ? TAKE_PHOTO
					: START_PREVIEW;
		}
	}

	private static enum LastVideoAction {
		STOP_RECORDING, START_RECORDING;
		public static LastVideoAction nextAction(LastVideoAction lastAction) {
			return START_RECORDING.equals(lastAction) ? STOP_RECORDING
					: START_RECORDING;
		}
	}

	private LastAction lastAction = LastAction.TAKE_PHOTO;
	private LastCameraAction lastCameraAction = null;
	private LastVideoAction lastVideoAction = null;
	public MediaRecorder mrec = null;

	private CameraMainActivity hideAll() {
		for (View view : new View[] { cameraPreview, videoPreview, imageViewer,
				videoViewer }) {
			try {
				view.setVisibility(View.INVISIBLE);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("\nError to set visible attribute for "
						+ view.toString());
			}
		}
		return this;
	}

	private void show(View view) {
		try {
			view.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("\nError to set visible attribute for "
					+ view.toString());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_main);
		frameLayout = (FrameLayout) findViewById(R.id.preview); 
		rlTakePhoto = (RelativeLayout) findViewById(R.id.rlCamere);
		rlTakeVideo = (RelativeLayout) findViewById(R.id.rlVideo); 
		tvTime = (TextView) findViewById(R.id.tvTime);

		cameraPreview = new CameraPreviewSurfaceView(this);
		videoPreview = new VideoPreviewSurfaceView(this);
		imageViewer = new ImageView(this);
		videoViewer = new VideoView(this);

		frameLayout.addView(cameraPreview);
		frameLayout.addView(videoPreview);
		frameLayout.addView(imageViewer);
		frameLayout.addView(videoViewer);
		this.hideAll();
		this.initCbutton();
		this.initVbutton();
		this.initBackbutton();
		this.initToButton();
		this.initChangeButton();
		this.initLightButton();

	}

	private void initChangeButton() {
		cButton.setClickable(true);
		btnChangeCamera = (Button) findViewById(R.id.btnChangeCamera);
		btnChangeCamera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!lastAction.equals(LastAction.TAKE_VIDEO)) {
					if (null != mrec) {
						mrec.reset();
						mrec.release();
						mrec = null;
					}
					CameraHolder.INSTANCE.releaseCamera(cameraPreview,
							videoPreview);
					CameraHolder.INSTANCE.changeCamera();
					// targetFilePath = null;
					CameraHolder.INSTANCE.requestCamera(cameraPreview);
					hideAll().show(cameraPreview);
					CameraHolder.INSTANCE.getCamera().startPreview();
					if (CameraHolder.INSTANCE.isFront()) {
						btnLight.setVisibility(View.INVISIBLE);
						btnToVideo.setVisibility(View.INVISIBLE);
					} else {
						btnLight.setVisibility(View.VISIBLE);
						btnToVideo.setVisibility(View.VISIBLE);
					}
				}
			}
		});
	} 

	private void initLightButton() {
		btnLight = (Button) findViewById(R.id.btnLight);
		btnLight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!CameraHolder.INSTANCE.isFront()) {
					if (!isLightOn) {
						lightOn();
						isLightOn = true;
					} else {
						lightOff();
						isLightOn = false;
					}
				}
			}
		});
		// this.lightOn();
	}

	private void initToButton() {
		btnToVideo = (Button) findViewById(R.id.btnToVideo);
		btnToVideo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				frameLayout.setOnTouchListener(null);
				btnLight.setVisibility(View.INVISIBLE);
				btnChangeCamera.setVisibility(View.INVISIBLE);
				rlTakePhoto.setVisibility(View.GONE);
				rlTakeVideo.setVisibility(View.VISIBLE);
				frameLayout.setOnTouchListener(null);
				try {
					starVideo();
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("star vedio failed");
				}
			}
		});
		btnToCamere = (Button) findViewById(R.id.btnToCamere);
		btnToCamere.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				frameLayout.setOnTouchListener(touchListener);
				rlTakePhoto.setVisibility(View.VISIBLE);
				rlTakeVideo.setVisibility(View.GONE);
				btnChangeCamera.setVisibility(View.VISIBLE);
				btnLight.setVisibility(View.VISIBLE);
				starCamera();
				// CameraHolder.INSTANCE.setDefaultCamera();// 设置为背面摄像头
			}
		});
	}

	private void initBackbutton() {
		Button backButton = (Button) findViewById(R.id.buttonBack);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (targetFilePath != null
						&& targetFilePath.trim().length() > 0) {
					File file = new File(targetFilePath);
					if (file.exists()) {
						file.delete();
						targetFilePath = null;
					}
				}
				finish();
			}
		});
	}

	public void starCamera() {
		lastCameraAction = LastCameraAction.nextAction(lastCameraAction);
		if (LastCameraAction.START_PREVIEW.equals(lastCameraAction)) {
			System.out.println("start preview");

			if (null != mrec) {
				mrec.reset();
				mrec.release();
				mrec = null;
				CameraHolder.INSTANCE.getCamera().lock();
			}
			CameraHolder.INSTANCE.releaseCamera(cameraPreview, videoPreview);
			CameraHolder.INSTANCE.requestCamera(cameraPreview);
			CameraMainActivity.this.cameraPreview.getHolder().setType(
					SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			CameraMainActivity.this.resetActionFlag(videoPreview);
			hideAll().show(cameraPreview);
			lastAction = LastAction.TAKE_PHOTO;
			CameraHolder.INSTANCE.getCamera().startPreview();
		}
	}

	private void initCbutton() {
		cButton = (Button) findViewById(R.id.btnPicture);
		cButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isMovie = 0;
				starCamera();
				if (LastCameraAction.TAKE_PHOTO.equals(lastCameraAction)) {
					System.out.println("take photo");

					targetFilePath = null;
					targetFilePath = pictureCallbackHandler.getOutputFile()
							.getAbsolutePath();
					uri = Uri.parse("file:///" + targetFilePath);
					System.out.println("taking");
					CameraHolder.INSTANCE.getCamera().takePicture(null, null,
							pictureCallbackHandler);
					// cButton.setClickable(false);
				}
			}
		});
	}

	PictureCallbackHandler pictureCallbackHandler = new PictureCallbackHandler(
			this);

	private void starVideo() {
		if (null != mrec) {
			mrec.reset();
			mrec.release();
			mrec = null;
		}
		CameraHolder.INSTANCE.releaseCamera(videoPreview, cameraPreview);
		targetFilePath = null;
		CameraHolder.INSTANCE.setDefaultCamera();// 设置为背面摄像头
		CameraHolder.INSTANCE.requestCamera(videoPreview);

		if (!lastAction.equals(LastAction.TAKE_VIDEO))
			hideAll().show(videoPreview);
		lastAction = LastAction.TAKE_VIDEO;
		CameraMainActivity.this.videoPreview.getHolder().setType(
				SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		CameraMainActivity.this.resetActionFlag(cameraPreview);

		targetFilePath = CameraHolder.getOutputMediaFile(
				OutputMediaFileType.MEDIA_TYPE_VIDEO).getAbsolutePath();

		CameraHolder.INSTANCE.getCamera().unlock();
		mrec = new MediaRecorder(); // Works well
		mrec.setCamera(CameraHolder.INSTANCE.getCamera());
		mrec.setPreviewDisplay(CameraMainActivity.this.videoPreview.getHolder()
				.getSurface());// 预览

		mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);// 视频源
		mrec.setAudioSource(MediaRecorder.AudioSource.MIC); // 录音源为麦克风
		mrec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);// 输出格式
		mrec.setOrientationHint(90);
		mrec.setVideoSize(640, 480); 
		mrec.setVideoEncodingBitRate(2 * 1024 * 1024); 
		mrec.setVideoFrameRate(10);  
		mrec.setVideoEncoder(MediaRecorder.VideoEncoder.H264);// 视频编码
		mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);// 音频编码
		mrec.setMaxDuration(15000);// 最大期限15s		
		mrec.setOutputFile(targetFilePath);// 保存路径 
		try {
			mrec.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}
		timeTask = new TimerTask() {
			@Override
			public void run() {

				runOnUiThread(new Runnable() { // UI thread
					@Override
					public void run() {
						if (timeCount < 10)
							tvTime.setText(timeStr + "0" + timeCount);
						else
							tvTime.setText(timeStr + timeCount);
						timeCount++;
					}
				});
			}
		};
	}

	private void initVbutton() {
		vButton = (Button) findViewById(R.id.btnTakeVideo);
		vButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isMovie = 1;
				try {
					lastVideoAction = LastVideoAction
							.nextAction(lastVideoAction);
					if (LastVideoAction.START_RECORDING.equals(lastVideoAction)) {
						// if (mrec == null)
						// starVideo();
						vButton.setText("暂停");
						if (mrec != null) {
							btnToCamere.setVisibility(View.GONE);
							task = new TimerTask() {
								public void run() {
									Message message = new Message();
									message.what = 1;
									handler.sendMessage(message);
								}
							};
							mrec.start();
							timer = new Timer(true);
							timerRe = new Timer(true);
							timeCount = 1;
							tvTime.setText(timeStr + "0" + timeCount);
							timer.schedule(task, 16000, 16000);
							timerRe.schedule(timeTask, 1000, 1000);
						}
					}
					if (LastVideoAction.STOP_RECORDING.equals(lastVideoAction)) {
						Message message = new Message();
						message.what = 1;
						handler.sendMessage(message);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("\nError to load local image: "
							+ e.getMessage());
				}
			}
		});

	}

	Timer timerRe = null;
	int timeCount = 0;
	String timeStr = "00:00:";
	TimerTask timeTask;
	Timer timer = null;
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			System.out.println("stop recording");
			btnToCamere.setVisibility(View.VISIBLE);
			vButton.setText("启动");
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
			if (timerRe != null) {
				timerRe.cancel();
				timerRe = null;
			}
			if (timeCount < 2) {
				File file = new File(targetFilePath);
				if (file.exists())
					file.delete();
				timeCount = 1;
				return;
			}
			if (null != mrec) {
				mrec.stop();
				mrec.reset();
				mrec.release();
				mrec = null;
			}
			CameraHolder.INSTANCE.getCamera().stopPreview();
			CameraHolder.INSTANCE.getCamera().lock();

			// 视频播放
			videoViewer.setVideoPath("file:///" + targetFilePath);
			videoViewer.setMediaController(new MediaController(
					CameraMainActivity.this));
			hideAll();
			show(videoViewer);
		}
	};

	// boolean isVisible = true;

	class TouchEvent implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) { 
			CameraHolder.INSTANCE.getCamera().autoFocus(autoFocusCallback);
			return false;
		}
	}

	AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			 
		}
	};

	public void lightOn() {
		Parameters parameters = CameraHolder.INSTANCE.getCamera()
				.getParameters();
		if (parameters == null) {
			return;
		}
		List<String> flashModes = parameters.getSupportedFlashModes();
		// Check if camera flash exists
		if (flashModes == null) {
			return;
		}
		String flashMode = parameters.getFlashMode();
		if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
			// Turn on the flash
			if (flashModes.contains(Parameters.FLASH_MODE_ON)) {
				parameters.setFlashMode(Parameters.FLASH_MODE_ON);
				CameraHolder.INSTANCE.getCamera().setParameters(parameters);
			} else {
			}
		}
	}

	public void lightOff() {
		Parameters parameters = CameraHolder.INSTANCE.getCamera()
				.getParameters();
		List<String> flashModes = parameters.getSupportedFlashModes();
		String flashMode = parameters.getFlashMode();
		// Check if camera flash exists
		if (flashModes == null) {
			return;
		}
		if (!Parameters.FLASH_MODE_OFF.equals(flashMode)) {
			// Turn off the flash
			if (flashModes.contains(Parameters.FLASH_MODE_OFF)) {
				parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
				CameraHolder.INSTANCE.getCamera().setParameters(parameters);
			} else {
				Log.e("TAG", "FLASH_MODE_OFF not supported");
			}
		}
	}

	@Override
	protected void onResume() {
		try {
			System.out.println("onResume");
			super.onResume();
			this.resetActionFlag(null);
			rlTakePhoto.setVisibility(View.VISIBLE);
			rlTakeVideo.setVisibility(View.GONE);
			btnChangeCamera.setVisibility(View.VISIBLE);
			// CameraHolder.INSTANCE.requestCamera(cameraPreview, videoPreview);
			// hideAll().show(cameraPreview);
			frameLayout.setOnTouchListener(touchListener);
			lastAction = LastAction.TAKE_PHOTO;
			starCamera();
			// lightOn();
			// isLightOn = true;
			isMovie = 0;
			cButton.setClickable(true);
			if (CameraHolder.INSTANCE.isFront()) {
				btnLight.setVisibility(View.INVISIBLE);
			} else {
				btnLight.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			System.out
					.println("\nError to load local image: " + e.getMessage());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// if (targetFilePath != null) {
		// File file = new File(targetFilePath);
		// if (file.exists())
		// file.delete();
		// }
		CameraHolder.INSTANCE.releaseCamera(cameraPreview, videoPreview);
	}

	@Override
	protected void onPause() {
		try {
			System.out.println("onPause");
			super.onPause();
			if (null != mrec) {
				mrec.reset();
				mrec.release();
				mrec = null;
			}
			hideAll();
			CameraHolder.INSTANCE.releaseCamera(cameraPreview, videoPreview);
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
		} catch (Exception e) {
			System.out
					.println("\nError to load local image: " + e.getMessage());
		}
	}

	private void resetActionFlag(AbstractPreviewSurfaceView view) {
		if (null == view) {
			lastCameraAction = LastCameraAction.TAKE_PHOTO;
			lastVideoAction = LastVideoAction.STOP_RECORDING;
		} else if (view instanceof CameraPreviewSurfaceView) {
			lastCameraAction = LastCameraAction.TAKE_PHOTO;
		} else if (view instanceof VideoPreviewSurfaceView) {
			lastVideoAction = LastVideoAction.STOP_RECORDING;
		}
	}
 
	public void toNext() {
		targetFilePath = uri.getPath(); 
		cropImageUri(uri, CROP_BIG_PICTURE);
	}

	public void cropImageUri(Uri uri, int requestCode) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("scale", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		intent.putExtra("return-data", false);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		startActivityForResult(intent, requestCode);
	} 
}