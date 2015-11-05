package com.example.camara;

import java.io.File;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class CamaraPrincipal extends Activity implements CvCameraViewListener2{

	private final int NUMERO_CAMARA=0;
	private boolean camaraFrontal, camaraTrasera;
	private Mat imagenOpenCV;
	private CameraBridgeViewBase mCameraView;
	private Boolean tomarFotoB, cambiarColor;
	private Camera mCamera;
	private List<Camera.Size> sizes;
	private Camera.Parameters params;
	
	
	private BaseLoaderCallback cargadorOpenCV= new BaseLoaderCallback(this){ 
		@Override
		public void onManagerConnected(final int status){
			if( status == LoaderCallbackInterface.SUCCESS){
				mCameraView.enableView();
				imagenOpenCV=new Mat();
			}
			else
				super.onManagerConnected(status);
		}
		
	};
	
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_camara_principal);
		final Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		tomarFotoB=false;
		cambiarColor = false;			
		
		CameraInfo cameraInfo=new CameraInfo();
		Camera.getCameraInfo(NUMERO_CAMARA, cameraInfo);
			
		//mCamera.release();
		//mCamera = Camera.open(NUMERO_CAMARA);
		//params = mCamera.getParameters();		
		//sizes = params.getSupportedPreviewSizes();
		
		camaraFrontal = cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT;
		camaraTrasera = cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK;
		
		mCameraView=new JavaCameraView (this, NUMERO_CAMARA);
		mCameraView.setCvCameraViewListener(this);
		
		setContentView(mCameraView);
	}

	public void onPause(){
		if(mCameraView!=null){
			mCameraView.disableView();
			
		}
		super.onPause();
		}
	
	public void onResume(){
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, cargadorOpenCV);
	}
	public void onDestroy(){
		if(mCameraView != null) {
			mCameraView.disableView();
		}
		super.onDestroy();
	}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.camara_principal, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id){
		case R.id.menu_tomar_foto:
			tomarFotoB=true;
			return true;
			
		case R.id.menu_change_color:
			if( cambiarColor )
				cambiarColor = false;
			else
				cambiarColor = true;
			return true;
			
		case R.id.menu_change_camera:
			
			if( camaraTrasera && !camaraFrontal){
				Toast.makeText(this,  "No existe una cámara frontal", Toast.LENGTH_LONG).show();
			}
			return true;
			
		case R.id.menu_change_resolution:
			changeResolution(mCameraView);
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		
	}

	@Override
	public void onCameraViewStopped() {
		
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		final Mat rgba=inputFrame.rgba();		
		final Mat imagenGris=new Mat();
				
		if(cambiarColor){
			Imgproc.cvtColor(rgba, imagenGris, Imgproc.COLOR_RGBA2GRAY);
			if(tomarFotoB){
				tomarFoto(imagenGris);
				tomarFotoB = false;				
			}
			
			return imagenGris;
			
		}else{
			if(tomarFotoB){
				tomarFoto(rgba);
				tomarFotoB = false;				
			}
			
			return rgba;
		}
		
		
	}
	
	private void tomarFoto(final Mat rgba){
		final String ruta=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()
				+File.separator+"Camara";
		File rutaImagen=new File(ruta);
		 
		if(!rutaImagen.isDirectory() && !rutaImagen.mkdirs()){
			Log.e("Camara", "Archivo no valido");
			return;
		}
		Imgproc.cvtColor(rgba, imagenOpenCV, Imgproc.COLOR_RGBA2BGR,3);
		if(!Imgcodecs.imwrite(ruta+File.separator+"imagen.bmp", imagenOpenCV)){
			Log.e("Camara", "Error al convertir imagen");
			return;
		}
		Log.v("com.example.camara", "Imagen guardada con exito");		
	}	
	
	@SuppressWarnings("deprecation")
	private void changeResolution(CameraBridgeViewBase camaraOCV){
		//mCamera=Camera.open();
		//Camera.Parameters params = mCamera.getParameters();
		  
		//List<Camera.Size> sizes = params.getSupportedPreviewSizes();
		 
		Camera.Size mSize = params.getPreviewSize();
		
		mSize = comparaResoluciones( mSize, sizes );					    
		 		
		//params.setPictureSize(mSize.width, mSize.height);
		//mCamera.setParameters(params);
		//mCamera.release();
		camaraOCV.setMaxFrameSize(mSize.width, mSize.height);
	}
	
	private Camera.Size comparaResoluciones(Camera.Size tamOriginal, List<Camera.Size> tams){
		int wOrig, hOrig, wNuevo, hNuevo;
		int resultOriginal, resultNuevo;
		
		Camera.Size nuevoTamanio = tamOriginal;
		
		wOrig = tamOriginal.width;
		hOrig = tamOriginal.height;
		resultOriginal = wOrig * hOrig;
		
		for (Camera.Size size : tams) {			
					
			wNuevo = size.width;
			hNuevo = size.height;
			resultNuevo = wNuevo * hNuevo;
			
		    if ( resultNuevo < resultOriginal ) {
		    	nuevoTamanio = size;
		        break;		       
		    }
		    
		}
		
		return nuevoTamanio;
		
	}
	
	
}
