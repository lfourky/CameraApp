package lazar.vladimir.ivkov.cameraapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
public class MainActivity extends Activity {

	public static final int MEDIA_TYPE_IMAGE = 1;
	private static String destinationFileName = "cameraApp"; //This is the name of the folder where the pics go
	private Camera mCamera;
	private CameraPreview mPreview;
	private FrameLayout preview;
	private SurfaceHolder mHolder;
	
	private boolean safeToTakePicture = false;
	
	

	public void takePicture(View v) {
		if (safeToTakePicture) {
			mCamera.takePicture(null, null, mPicture);
			Toast.makeText(getApplicationContext(), "Picture taken.",Toast.LENGTH_SHORT).show();
			safeToTakePicture = false;
		}
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	
		initializeCamera();
	}


	
	private void initializeCamera() {
		mCamera = getCameraInstance();

		mPreview = new CameraPreview(this, mCamera);
		preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		preview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				takePicture(v);
			}
		});
	}

	

	/** Create a file Uri for saving an image or video */
	@SuppressWarnings("unused")
	private Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	@SuppressLint("SimpleDateFormat")
	private static File getOutputMediaFile(int type) {

		String root = Environment.getExternalStorageDirectory().toString();
		File myDir = new File(root + "/"+destinationFileName);
		myDir.mkdirs();

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) 
			mediaFile = new File(myDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
		 else 
			return null;
		

		return mediaFile;
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			camera.startPreview();

			if (pictureFile == null) {
				safeToTakePicture = true;
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {	e.printStackTrace();  }
			  catch (IOException e)           { e.printStackTrace();  }
			
			
			safeToTakePicture = true;
		}
	};

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return c;
	}

	
	@Override
	protected void onResume() {
		super.onResume();

		if (mCamera == null) {
			initializeCamera();
		}
		mCamera.startPreview();
		mHolder = mPreview.getHolder();
		mHolder.setSizeFromLayout();

	}

	@Override
	protected void onPause() {
		super.onPause();
		
		mCamera.stopPreview();
		mPreview.getHolder().removeCallback(mPreview);
		mCamera.setPreviewCallback(null);
		mCamera.release();
		mCamera = null;
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	    private SurfaceHolder mHolder;
	    private Camera mCamera;

	    @SuppressWarnings("deprecation")
		public CameraPreview(Context context, Camera camera) {
	        super(context);
	        mCamera = camera;

	        mHolder = getHolder();
	        mHolder.addCallback(this);
	        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    }

	    public void surfaceCreated(SurfaceHolder holder) {
	        // The Surface has been created, now tell the camera where to draw the preview.
	        try {
	            mCamera.setPreviewDisplay(holder);
	            mCamera.startPreview();
	        } catch (IOException e) { e.printStackTrace();  }
	    }

	    public void surfaceDestroyed(SurfaceHolder holder) {

	    }

	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
	    	
	        if (mHolder.getSurface() == null){
	          return;
	        }

	        try {
	            mCamera.stopPreview();
	        } catch (Exception e) { e.printStackTrace();  }
	        
	  
	        try {
	            mCamera.setPreviewDisplay(mHolder);
	            Camera.Parameters params = mCamera.getParameters();
	            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
	            params.setJpegQuality(100);
	            mCamera.setParameters(params);
	            mCamera.setDisplayOrientation(90);
	            
	            mCamera.startPreview();
	        	safeToTakePicture = true;

	        } catch (Exception e){
	        }
	        
	    }
	   
	}

}
