package com.mou.robotremote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.thinkgear.*;
import java.io.*; 
import java.net.*;
import android.view.View.*;
import android.widget.*;


public class robotremote extends Activity {
	BluetoothAdapter bluetoothAdapter;
	
	TextView tv, Meditation,Attention;
	Button b;
	ScrollView scrollview;
	
	TGDevice tgDevice;
	final boolean rawEnabled = false;
	
	DatagramSocket s;
	InetAddress local;
	
	public void udpSend(String server,int server_port,String messageStr){
		try
		{
			s = new DatagramSocket();
		}
		catch (SocketException e)
		{Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT);
		}
		try
		{
			local = InetAddress.getByName(server);
		}
		catch (UnknownHostException e)
		{Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT);
		}
		int msg_length = messageStr.length();
		byte[] message = messageStr.getBytes();
		DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);
		try
		{
			s.send(p);
		}
		catch (IOException e)
		{Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT);}
		
	}
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tv = (TextView)findViewById(R.id.log);
		b = (Button)findViewById(R.id.connect);
        tv.append("Android version: " + Integer.valueOf(android.os.Build.VERSION.SDK) + "\n" );
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		scrollview = (ScrollView) findViewById(R.id.scrollView1);
		
		
		
		final Button forward = (Button) findViewById(R.id.Forward);
		final Button backward = (Button) findViewById(R.id.Backward);
		final Button left = (Button) findViewById(R.id.Left);
		final Button right = (Button) findViewById(R.id.Right);
		
		Meditation = (TextView) findViewById(R.id.TVM);
		Attention = (TextView) findViewById(R.id.TVA);
		
		
		
		right.setOnClickListener(new OnClickListener(){
			public void onClick(View p){
				udpSend("127.0.0.1",12345,"Mright");
				tv.append("UDP SENT: Mright\n");
				
			}
		});
		left.setOnClickListener(new OnClickListener(){
			public void onClick(View p){
				udpSend("127.0.0.1",12345,"Mleft");
				tv.append("UDP SENT: Mleft\n");
			}
		});
		forward.setOnClickListener(new OnClickListener(){
			public void onClick(View p){
				udpSend("127.0.0.1",12345,"Mforward");
				tv.append("UDP SENT: Mforward\n");
			}
		});
		backward.setOnClickListener(new OnClickListener(){
			public void onClick(View p){
				udpSend("127.0.0.1",12345,"Mbackward");
				tv.append("UDP SENT: Mbackward\n");
			}
		});
		
		
		//udpSend("127.0.0.1",12345,"test");
		
		b.setOnClickListener(new OnClickListener(){
			public void onClick(View p){
				tgDevice.connect(rawEnabled);
			}
		});
        if(bluetoothAdapter == null) {
        	// Alert user that Bluetooth is not available
        	Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
        	finish();
        	return;
        }else {
        	/* create the TGDevice */
        	tgDevice = new TGDevice(bluetoothAdapter, handler);
        }  
    }
    
    @Override
    public void onDestroy() {
    	tgDevice.close();
        super.onDestroy();
    }
    /**
     * Handles messages from TGDevice
     */
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
			/*
			scrollview.post(new Runnable() {
				@Override
				public void run() {
					scrollview.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
			*/
			try{
				scrollview.fullScroll(View.FOCUS_DOWN);
			}catch(Exception e){}
        	switch (msg.what) {
            case TGDevice.MSG_STATE_CHANGE:

                switch (msg.arg1) {
	                case TGDevice.STATE_IDLE:
	                    break;
	                case TGDevice.STATE_CONNECTING:		                	
	                	tv.append("Connecting...\n");
	                	break;		                    
	                case TGDevice.STATE_CONNECTED:
	                	tv.append("Connected.\n");
	                	tgDevice.start();
	                    break;
	                case TGDevice.STATE_NOT_FOUND:
	                	tv.append("Can't find\n");
	                	break;
	                case TGDevice.STATE_NOT_PAIRED:
	                	tv.append("not paired\n");
	                	break;
	                case TGDevice.STATE_DISCONNECTED:
	                	tv.append("Disconnected mang\n");
                }

                break;
            case TGDevice.MSG_POOR_SIGNAL:
            		//signal = msg.arg1;
            		tv.append("PoorSignal: " + msg.arg1 + "\n");
                break;
            case TGDevice.MSG_RAW_DATA:	  
            		//raw1 = msg.arg1;
            		//tv.append("Got raw: " + msg.arg1 + "\n");
            	break;
            case TGDevice.MSG_HEART_RATE:
        		tv.append("Heart rate: " + msg.arg1 + "\n");
                break;
            case TGDevice.MSG_ATTENTION:
            	    //att = msg.arg1;
            		Attention.setText("A:"+msg.arg1);
            		//Log.v("HelloA", "Attention: " + att + "\n");
            	break;
            case TGDevice.MSG_MEDITATION:
				    Meditation.setText("M:"+msg.arg1);
					
					if (msg.arg1>70){
						udpSend("127.0.0.1",12345,"Mforward");
						tv.append("* UDP SENT: Mforward\n");
					}
					
            	break;
            case TGDevice.MSG_BLINK:
            		tv.append("Blink: " + msg.arg1 + "\n");
            	break;
            case TGDevice.MSG_RAW_COUNT:
            		//tv.append("Raw Count: " + msg.arg1 + "\n");
            	break;
            case TGDevice.MSG_LOW_BATTERY:
            	Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
            	break;
            case TGDevice.MSG_RAW_MULTI:
            	//TGRawMulti rawM = (TGRawMulti)msg.obj;
            	//tv.append("Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
            default:
            	break;
        }
        }
    };
    
    public void doStuff(View view) {
    	if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED){
    		tgDevice.connect(rawEnabled);
		}
    	//tgDevice.ena
    }
}
