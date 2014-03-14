package com.haraye.friendlocation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.haraye.friendlocation.interfaces.IAppManager;
import com.haraye.friendlocation.service.FriendLocationService;




public class SignUp extends Activity {
	
	private static final int FILL_ALL_FIELDS = 0;
	protected static final int TYPE_SAME_PASSWORD_IN_PASSWORD_FIELDS = 1;
	private static final int SIGN_UP_FAILED = 2;
	private static final int SIGN_UP_USERNAME_CRASHED = 3;
	private static final int SIGN_UP_SUCCESSFULL = 4;
	protected static final int USERNAME_AND_PASSWORD_LENGTH_SHORT = 5;
	
	
//	private static final String SERVER_RES_SIGN_UP_FAILED = "0";
	private static final String SERVER_RES_RES_SIGN_UP_SUCCESFULL = "1";
	private static final String SERVER_RES_SIGN_UP_USERNAME_CRASHED = "2";
	
//	private Context mCtx = SignUp.this;
	private String TAG = "NewUserRegister";
	
	private EditText userdnameText;
	private EditText usernameText;
	private EditText passwordText;
	private EditText eMailText;
//	private EditText passwordAgainText;
	private IAppManager imService;
	private Handler handler = new Handler();
	
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
            Toast.makeText(SignUp.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };

	public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);    

	    Common.fullScreenMode(SignUp.this);
	               
	        setContentView(R.layout.newuser);
	       // setTitle("Sign up");
	        
	        Button signUpButton = (Button) findViewById(R.id.signUp); //signUp
	        Button cancelButton = (Button) findViewById(R.id.cancel_signUp); //cancel_signUp
	        userdnameText = (EditText)findViewById(R.id.edtname);
	        usernameText = (EditText) findViewById(R.id.edtuser); //userName
	        passwordText = (EditText) findViewById(R.id.edtpass);  //password
	        eMailText = (EditText) findViewById(R.id.edtemail); //email
	        
	        signUpButton.setOnClickListener(new OnClickListener(){
				public void onClick(View arg0) 
				{						
					if (userdnameText.length() > 0 &&
						usernameText.length() > 0 &&		
						passwordText.length() > 0 && 
		//				passwordAgainText.length() > 0 &&
						eMailText.length() > 0
						)
					{
						//TODO check email address is valid
						
						if (Common.isValidEmail(eMailText.getText().toString())){
						
							if (usernameText.length() >= 5 && passwordText.length() >= 5) {
							
									Thread thread = new Thread(){
										String result = new String();
										@Override
										public void run() {
											Log.e(TAG, " "+userdnameText.getText().toString());
											Log.e(TAG, " "+usernameText.getText().toString());
											Log.e(TAG, " "+passwordText.getText().toString());
											Log.e(TAG, " "+eMailText.getText().toString());
											
											
											result = imService.signUpUser(userdnameText.getText().toString(),
													usernameText.getText().toString(), 
													passwordText.getText().toString(), 
													eMailText.getText().toString(),
													"app_icon.png");
		
											handler.post(new Runnable(){
		
												public void run() {
													Log.e("RESULT Signup", " "+result);
													String[] res = result.split(":");
													result=res[0];
													if (result.equals(SERVER_RES_RES_SIGN_UP_SUCCESFULL)) {
														Toast.makeText(getApplicationContext(),R.string.signup_successfull, Toast.LENGTH_LONG).show();
														Intent i = new Intent(SignUp.this, Login.class);
														//i.putExtra("id", res[1]);
														startActivity(i);
													finish();
														//showDialog(SIGN_UP_SUCCESSFULL);
													}
													else if (result.equals(SERVER_RES_SIGN_UP_USERNAME_CRASHED)){
														Toast.makeText(getApplicationContext(),R.string.signup_username_crashed, Toast.LENGTH_LONG).show();
														//showDialog(SIGN_UP_USERNAME_CRASHED);
													}
													else  //if (result.equals(SERVER_RES_SIGN_UP_FAILED)) 
													{
														Toast.makeText(getApplicationContext(),R.string.signup_failed, Toast.LENGTH_LONG).show();
														//showDialog(SIGN_UP_FAILED);
													}			
												}
		
											});
										}
		
									};
									thread.start();
							}
							else{
								Toast.makeText(getApplicationContext(),R.string.username_and_password_length_short, Toast.LENGTH_LONG).show();
								//showDialog(USERNAME_AND_PASSWORD_LENGTH_SHORT);
							}							
						}
						else {
							Toast.makeText(getApplicationContext(),R.string.signup_email_invalid, Toast.LENGTH_LONG).show();
							//showDialog(TYPE_SAME_PASSWORD_IN_PASSWORD_FIELDS);
						}
						
					}
					else {
						Toast.makeText(getApplicationContext(),R.string.signup_fill_all_fields, Toast.LENGTH_LONG).show();
						//showDialog(FILL_ALL_FIELDS);
						
					}				
				}       	
	        });
	        
	        cancelButton.setOnClickListener(new OnClickListener(){
				public void onClick(View arg0) 
				{						
					finish();					
				}	        	
	        });
	        
	        
	    }
	
	
	protected Dialog onCreateDialog(int id) 
	{    	
		  	
		switch (id) 
		{
			case TYPE_SAME_PASSWORD_IN_PASSWORD_FIELDS:			
				return new AlertDialog.Builder(SignUp.this)       
				.setMessage(R.string.signup_type_same_password_in_password_fields)
				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						/* User clicked OK so do some stuff */
					}
				})        
				.create();			
			case FILL_ALL_FIELDS:				
				return new AlertDialog.Builder(SignUp.this)       
				.setMessage(R.string.signup_fill_all_fields)
				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						/* User clicked OK so do some stuff */
					}
				})        
				.create();
			case SIGN_UP_FAILED:
				return new AlertDialog.Builder(SignUp.this)       
				.setMessage(R.string.signup_failed)
				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						/* User clicked OK so do some stuff */
					}
				})        
				.create();
			case SIGN_UP_USERNAME_CRASHED:
				return new AlertDialog.Builder(SignUp.this)       
				.setMessage(R.string.signup_username_crashed)
				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						/* User clicked OK so do some stuff */
					}
				})        
				.create();
			case SIGN_UP_SUCCESSFULL:
				return new AlertDialog.Builder(SignUp.this)       
				.setMessage(R.string.signup_successfull)
				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						finish();
					}
				})        
				.create();	
			case USERNAME_AND_PASSWORD_LENGTH_SHORT:
				return new AlertDialog.Builder(SignUp.this)       
				.setMessage(R.string.username_and_password_length_short)
				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						/* User clicked OK so do some stuff */
					}
				})        
				.create();
			default:
				return null;
				
		}

	
	}
	
	@Override
	protected void onResume() {
		bindService(new Intent(SignUp.this, FriendLocationService.class), mConnection , Context.BIND_AUTO_CREATE);
		   
		super.onResume();
	}
	
	@Override
	protected void onPause() 
	{
		unbindService(mConnection);
		super.onPause();
	}
	
	

}
