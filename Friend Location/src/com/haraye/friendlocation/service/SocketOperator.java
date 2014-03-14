package com.haraye.friendlocation.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.haraye.friendlocation.interfaces.IAppManager;
import com.haraye.friendlocation.interfaces.ISocketOperator;


public class SocketOperator implements ISocketOperator
{
	public static final String AUTHENTICATION_SERVER_ADDRESS = "http://www.harayetech.com/friendlocationservice/"; //TODO change to your WebAPI Address
	public static final String AUTHENTICATION_SERVER_UPLOAD_ADDRESS = "http://www.harayetech.com/friendlocationservice/upload/"; //TODO change to your WebAPI Address
	private int listeningPort = 0;
	
//	private static final String HTTP_REQUEST_FAILED = null;
	
	private HashMap<InetAddress, Socket> sockets = new HashMap<InetAddress, Socket>();
	
	private ServerSocket serverSocket = null;
	
	static JSONObject jObj;
    static String json = "";

	private boolean listening;

	private class ReceiveConnection extends Thread {
		Socket clientSocket = null;
		public ReceiveConnection(Socket socket) 
		{
			this.clientSocket = socket;
			SocketOperator.this.sockets.put(socket.getInetAddress(), socket);
		}
		
		@Override
		public void run() {
			 try {
	//			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(
						    new InputStreamReader(
						    		clientSocket.getInputStream()));
				String inputLine;
				
				 while ((inputLine = in.readLine()) != null) 
				 {
					 if (inputLine.equals("exit") == false)
					 {
						 //appManager.messageReceived(inputLine);						 
					 }
					 else
					 {
						 clientSocket.shutdownInput();
						 clientSocket.shutdownOutput();
						 clientSocket.close();
						 SocketOperator.this.sockets.remove(clientSocket.getInetAddress());
					 }						 
				 }		
				
			} catch (IOException e) {
				Log.e("ReceiveConnection.run: when receiving connection ","");
			}			
		}	
	}

	public SocketOperator(IAppManager appManager) {	
	}
	
	
	/*public String sendHttpRequest(String params)
	{		
		URL url;
		String result = new String();
		try 
		{
			url = new URL(AUTHENTICATION_SERVER_ADDRESS);
			HttpURLConnection connection;
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			
			PrintWriter out = new PrintWriter(connection.getOutputStream());
			
			out.println(params);
			out.close();

			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							connection.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				result = result.concat(inputLine);				
			}
			in.close();			
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}			
		
		if (result.length() == 0) {
			result = HTTP_REQUEST_FAILED;
		}
		
		return result;
		
	
	}

*/
	public JSONObject sendHttpRequestJson(String params)
	{		
		URL url;
		
		try 
		{
			url = new URL(AUTHENTICATION_SERVER_ADDRESS);
			HttpURLConnection connection;
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			
			PrintWriter out = new PrintWriter(connection.getOutputStream());
			
			out.println(params);
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(
							connection.getInputStream()));
			
			StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = in.readLine()) != null) {
                sb.append(line + "\n");
            }
			
			in.close();			
			json = sb.toString();
			Log.v(" SOcket Response ", " "+json);
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}			
		
		 try {
	            jObj = new JSONObject(json);
	        } catch (JSONException e) {
	            Log.e("JSON Parser send", "Error parsing data " + e.toString());
	        }
	 
	        // return JSON String
	        return jObj;
	
	}
	public static JSONObject makeHttpRequestJson(String params)
	{		
		URL url;
		
		try 
		{
			url = new URL(AUTHENTICATION_SERVER_ADDRESS);
			HttpURLConnection connection;
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			
			PrintWriter out = new PrintWriter(connection.getOutputStream());
			
			out.println(params);
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(
							connection.getInputStream()));
			
			StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = in.readLine()) != null) {
                sb.append(line + "\n");
            }
			
			in.close();			
			json = sb.toString();
			Log.v(" SOcket Response ", " "+json);
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}			
		
		 try {
	            jObj = new JSONObject(json);
	        } catch (JSONException e) {
	            Log.e("JSON Parser", "Error parsing data " + e.toString());
	        }
	 
	        // return JSON String
	        return jObj;
	
	}

	 public String makeHttpRequest(List<NameValuePair> params) {
		  InputStream is = null;
		  String result = null;
	        // Making HTTP request
	        try {
	 
	        	DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(AUTHENTICATION_SERVER_ADDRESS);
                httpPost.setEntity(new UrlEncodedFormEntity(params));
 
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
	                     
	 
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        } catch (ClientProtocolException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	 
	        try {
	            BufferedReader reader = new BufferedReader(new InputStreamReader(
	                    is, "iso-8859-1"), 8);
	            StringBuilder sb = new StringBuilder();
	            String line = null;
	            while ((line = reader.readLine()) != null) {
	            	//result = result.concat(line);	
	            	sb.append(line + "\n");
	            }
	            is.close();
	            result = sb.toString();
	        } catch (Exception e) {
	            Log.e("Buffer Error", "Error converting result " + e.toString());
	        }
	 
	 
	        // return JSON String
	        return result;
	        
	    }

	public int startListening(int portNo) 
	{
		listening = true;
		
		try {
			serverSocket = new ServerSocket(portNo);
			this.listeningPort = portNo;
		} catch (IOException e) {			
			
			//e.printStackTrace();
			this.listeningPort = 0;
			return 0;
		}

		while (listening) {
			try {
				new ReceiveConnection(serverSocket.accept()).start();
				
			} catch (IOException e) {
				//e.printStackTrace();				
				return 2;
			}
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {			
			Log.e("Exception server socket", "Exception when closing server socket");
			return 3;
		}
		
		
		return 1;
	}
	
	
	public void stopListening() 
	{
		this.listening = false;
	}
	
	public void exit() 
	{			
		for (Iterator<Socket> iterator = sockets.values().iterator(); iterator.hasNext();) 
		{
			Socket socket = (Socket) iterator.next();
			try {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			} catch (IOException e) 
			{				
			}		
		}
		
		sockets.clear();
		this.stopListening();
	}


	public int getListeningPort() {
		
		return this.listeningPort;
	}	

}
