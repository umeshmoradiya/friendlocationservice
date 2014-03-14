package com.haraye.friendlocation;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.haraye.friendlocation.interfaces.IAppManager;
import com.haraye.friendlocation.service.FriendLocationService;
import com.haraye.friendlocation.service.SocketOperator;

public class ImageTag extends Activity implements OnClickListener  {

	//keep track of camera capture intent
	final int CAMERA_CAPTURE = 1;
	//captured picture uri
	private Uri picUri;
	Bitmap pic;
	private IAppManager imService;
	String id;
	//keep track of cropping intent
	final int PIC_CROP = 2;
	
private ServiceConnection mConnection = new ServiceConnection() {
        

		public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            imService = ((FriendLocationService.IMBinder)service).getService();  
            
            
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
        	imService = null;
            
        }
    };
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    

    Common.fullScreenMode(ImageTag.this);
               
        setContentView(R.layout.screen_image_tag);
        
      //retrieve a reference to the UI button
    	Button captureBtn = (Button)findViewById(R.id.capture_btn);
    	Button saveBtn = (Button)findViewById(R.id.setpic);
    	Bundle extras = this.getIntent().getExtras();
		id = extras.getString("id");
    	//handle button clicks
    	captureBtn.setOnClickListener(this);
    	saveBtn.setOnClickListener(this);
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.capture_btn) {
			try {
			    //use standard intent to capture an image
			    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			    //we will handle the returned data in onActivityResult
			    startActivityForResult(captureIntent, CAMERA_CAPTURE);
			}catch(ActivityNotFoundException anfe){
			    //display an error message
			    String errorMessage = "Whoops - your device doesn't support capturing images!";
			    Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			    toast.show();
			}
	    }
		if(v.getId() == R.id.setpic){
			Intent i = new Intent(ImageTag.this,Login.class);
			 startActivity(i);
finish();
			
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == RESULT_OK) {
	    	//user is returning from capturing an image using the camera
	    	if(requestCode == CAMERA_CAPTURE){
	    		
	    		//get the Uri for the captured image
	    		picUri = data.getData();
	    		//carry out the crop operation
	    		performCrop();
	    	}
	    	//user is returning from cropping the image
	    	else if(requestCode == PIC_CROP){
	    		
	    		//get the returned data
	    		Bundle extras = data.getExtras();
	    		//get the cropped bitmap
	    		Bitmap thePic = extras.getParcelable("data");
	    		String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/saved_images");    
                myDir.mkdirs();
                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String fname = "Image-"+ n +".jpg";
                File file = new File (myDir, fname);
                if (file.exists ()) file.delete (); 
                try {
                   FileOutputStream out = new FileOutputStream(file);
                   thePic.compress(Bitmap.CompressFormat.JPEG, 100, out);
                   out.flush();
                   out.close();
                } catch (Exception e) {
                   e.printStackTrace();
                }
            

            File f = new File(picUri.getPath());            

            if (f.exists()) f.delete();
	    		
	    		
	    	//	String imgPic = AppUtil.bitmaptoString(thePic);
	    		
	    	//	File myFile = new File(picUri.toString());

	    		
	    		Log.e("PIC URI"," "+file.getPath());
	    		new UploadImage().execute(id,file.getPath());
	    	//	imService.updateImage(id, imgPic);
	    	//	pic = thePic;
	    		//retrieve a reference to the ImageView
	    	//	ImageView picView = (ImageView)findViewById(R.id.imageView1);
	    		//display the returned cropped image
	    	//	picView.setImageBitmap(thePic);
	    	}
	    }
	}
	
	public String getRealPathFromURI(Uri contentUri) 
	{
	     String[] proj = { MediaStore.Audio.Media.DATA };
	     Cursor cursor = managedQuery(contentUri, proj, null, null, null);
	     int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
	     cursor.moveToFirst();
	     return cursor.getString(column_index);
	}
	 public class UploadImage extends AsyncTask<String, Void, String>{

		 @Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			/* ImageView picView = (ImageView)findViewById(R.id.imageView1);
	    		//display the returned cropped image
	    		picView.setImageURI(Uri.parse(result));*/
			 super.onPostExecute(result);
			 Intent i = new Intent(ImageTag.this,Login.class);
			 startActivity(i);
finish();
		}
		@Override
		protected String doInBackground(String... params) {
			
			
			// TODO Auto-generated method stub
			String filePath = doFileUpload(params[1]);
			if(filePath!=null){
				String abd = imService.updateImage(params[0], filePath);
				Log.e("IMAGE TAG"," "+abd);
			}
			//String abd = imService.updateImage(params[0], params[1]);
			//Log.e("IMAGE TAG"," "+abd);
			return params[1];
		}}
	
	private void performCrop(){
		
		try {
			//call the standard crop action intent (the user device may not support it)
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			    //indicate image type and Uri
			cropIntent.setDataAndType(picUri, "image/*");
			    //set crop properties
			cropIntent.putExtra("crop", "true");
			    //indicate aspect of desired crop
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			    //indicate output X and Y
			cropIntent.putExtra("outputX", 1024);
			cropIntent.putExtra("outputY", 1024);
			    //retrieve data on return
			cropIntent.putExtra("scale", true);
			cropIntent.putExtra("return-data", true);
			cropIntent.putExtra("noFaceDetection", false);
			
			    //start the activity - we handle returning in onActivityResult
			startActivityForResult(cropIntent, PIC_CROP);
		}
		catch(ActivityNotFoundException anfe){
		    //display an error message
		    String errorMessage = "Whoops - your device doesn't support the crop action!";
		    Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
		    toast.show();
		}
	}
	
	@Override
	protected void onResume() {
		bindService(new Intent(ImageTag.this, FriendLocationService.class), mConnection , Context.BIND_AUTO_CREATE);
		   
		super.onResume();
	}
	
	@Override
	protected void onPause() 
	{
		unbindService(mConnection);
		super.onPause();
	}
	
	private String doFileUpload(String picPath){
		String fileName = null;
	    HttpURLConnection conn = null;
	    DataOutputStream dos = null;
	    DataInputStream inStream = null; 
	    String exsistingFileName = picPath;
	    // Is this the place are you doing something wrong.
	    String lineEnd = "\r\n";
	    String twoHyphens = "--";
	    String boundary =  "*****";
	    int bytesRead, bytesAvailable, bufferSize;
	    byte[] buffer;
	    int maxBufferSize = 1*1024*1024;
	    String urlString = SocketOperator.AUTHENTICATION_SERVER_ADDRESS+"upload.php";
	    try
	    {
	        Log.e("MediaPlayer","Inside second Method");
	        FileInputStream fileInputStream = new FileInputStream(new File(exsistingFileName) );
	        URL url = new URL(urlString);
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setDoInput(true);
	        // Allow Outputs
	        conn.setDoOutput(true);
	        // Don't use a cached copy.
	        conn.setUseCaches(false);
	        // Use a post method.
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Connection", "Keep-Alive");
	        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
	        dos = new DataOutputStream( conn.getOutputStream() );
	        dos.writeBytes(twoHyphens + boundary + lineEnd);
	        dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + exsistingFileName +"\"" + lineEnd);
	        dos.writeBytes(lineEnd);
	        Log.e("MediaPlayer","Headers are written");
	        bytesAvailable = fileInputStream.available();
	        bufferSize = Math.min(bytesAvailable, maxBufferSize);
	        buffer = new byte[bufferSize];
	        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	        while (bytesRead > 0)
	        {
	            dos.write(buffer, 0, bufferSize);
	            bytesAvailable = fileInputStream.available();
	            bufferSize = Math.min(bytesAvailable, maxBufferSize);
	            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	        }
	        dos.writeBytes(lineEnd);
	        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        String inputLine;
	        StringBuilder tv = new StringBuilder();
	        while ((inputLine = in.readLine()) != null) 
	            tv.append(inputLine);
	        // close streams
	        Log.e("MediaPlayer","File is written");
	        Log.e("MediaPlayer returns"," "+tv.toString());
	        
	        fileInputStream.close();
	        dos.flush();
	        dos.close();
	        fileName = tv.toString();
	    }
	    catch (MalformedURLException ex)
	    {
	        Log.e("MediaPlayer", "error: " + ex.getMessage(), ex);
	    }
	    catch (IOException ioe)
	    {
	        Log.e("MediaPlayer", "error: " + ioe.getMessage(), ioe);
	    }

	    //------------------ read the SERVER RESPONSE
	    try {
	        inStream = new DataInputStream ( conn.getInputStream() );
	        String str;            
	        while (( str = inStream.readLine()) != null)
	        {
	            Log.e("MediaPlayer","Server Response"+str);
	        }
	        /*while((str = inStream.readLine()) !=null ){

	        }*/
	        inStream.close();
	    }
	    catch (IOException ioex){
	        Log.e("MediaPlayer", "error: " + ioex.getMessage(), ioex);
	    }
	    return fileName;
	}

}
