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


import com.zerokol.views.JoystickView;
import com.zerokol.views.JoystickView.OnJoystickMoveListener;
import com.neurosky.thinkgear.*;

import java.io.*; 
import java.net.*;
import android.view.View.*;
import android.widget.*;
import android.view.*;
import android.content.*;
import android.webkit.*;
import android.os.*;


public class robotremote extends Activity {
	BluetoothAdapter bluetoothAdapter;
	
	TextView tv, Meditation,Attention;
	Button connect;
	ScrollView scrollview;
	private JoystickView joystick, camjoystick;
	
	boolean inMain = true;
	
	boolean lastCommandMind = false;
	
	boolean udpInUse = false;
	
	TGDevice tgDevice;
	final boolean rawEnabled = false;
	
	CountDownTimer autorefresh;
	
	String ip;
	int MinMeditation, port;
	
	int Vang, Hang;
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		Vang = 90;
		Hang = 90;
		
		
        tv = (TextView)findViewById(R.id.log);
		connect = (Button)findViewById(R.id.connect);
		
        tv.append("Android version: " + Integer.valueOf(android.os.Build.VERSION.SDK) + "\n" );
		
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		scrollview = (ScrollView) findViewById(R.id.logScroll);
		Saver saver = new Saver();
		
		String file;
		String fs[];
		file = saver.readFromFile(getApplicationContext());
		
		if (file.equals("")){
			port = 12345;
			ip = "127.0.0.1";
			MinMeditation = 70;
			saver.writeToFile(getApplicationContext(),ip+"\n"+Integer.toString(port)+"\n"+Integer.toString(MinMeditation));
		}
		else{
			fs = file.split("\n");
			ip = fs[0];
			port = Integer.parseInt(fs[1]);
			MinMeditation = Integer.parseInt(fs[2]);
		}
		tv.append("\nDest ip: "+ip);
		tv.append("\nPort: "+port);
		tv.append("\nMin meditation: "+MinMeditation+"\n");
		
		
		final WebView engine = (WebView) findViewById(R.id.capteurs);
		
		Meditation = (TextView) findViewById(R.id.TVM);
		Attention = (TextView) findViewById(R.id.TVA);
		
		
		autorefresh = new CountDownTimer(10000,1000){
			//auto reload web page of sensors
			public void onTick(long millis){
				
			}
			public void onFinish(){
				try{
					if (!udpInUse){
						try{
							Toast.makeText(getApplicationContext(),"Reloading sensors",Toast.LENGTH_LONG).show();
							engine.loadUrl("http://"+ip);
						}catch(Exception e){}
					}
				}catch(Exception e){}
				autorefresh.start();
			}
		};
		autorefresh.start();
		
		
		joystick = (JoystickView) findViewById(R.id.Joystick);
		camjoystick = (JoystickView) findViewById(R.id.CamJoystick);
		
		
        //Event listener that always returns the variation of the angle in degrees, motion power in percentage and direction of movement
        joystick.setOnJoystickMoveListener(new OnJoystickMoveListener() {
			@Override
			public void onValueChanged(int angle, int power, int direction) {
				udpInUse = true;
				udp Udp = new udp();
				
				switch (direction) {
					case JoystickView.FRONT:
						Udp.udpSend(ip,port,"Mforward");
						break;
					case JoystickView.RIGHT:
						Udp.udpSend(ip,port,"Mright");
						break;
					case JoystickView.BOTTOM:
						Udp.udpSend(ip,port,"Mbackward");
						break;
					case JoystickView.LEFT:
						Udp.udpSend(ip,port,"Mleft");
						break;
					default:
						Udp.udpSend(ip,port,"Mstop");
						udpInUse = false;
				}
			}
		},JoystickView.DEFAULT_LOOP_INTERVAL);
		
		camjoystick.setOnJoystickMoveListener(new OnJoystickMoveListener() {
				@Override
				public void onValueChanged(int angle, int power, int direction) {
					udpInUse = true;
					udp Udp = new udp();
					

					switch (direction) {
						case JoystickView.FRONT:
							Vang += power/15;
							break;
						case JoystickView.FRONT_RIGHT:
							Vang += power/15;
							Hang += power/15;
							break;
						
						case JoystickView.RIGHT:
							Hang += power/15;
							break;
							
						case JoystickView.RIGHT_BOTTOM:
							Hang += power/15;
							Vang -= power/15;
							break;
							
						case JoystickView.BOTTOM:
							Vang -= power/15;
							break;
						case JoystickView.BOTTOM_LEFT:
							Vang -= power/15;
							Hang -= power/15;
							break;
						case JoystickView.LEFT:
							Hang -= power/15;
							break;
						case JoystickView.LEFT_FRONT:
							Hang -= power/15;
							Vang += power/15;
							break;
						default:
						    Hang = 90;
							Vang = 90;
							udpInUse = false;
					}
					Udp.udpSend(ip,port,"C"+Integer.toString(Vang)+":"+Integer.toString(Hang));
				}
			},JoystickView.DEFAULT_LOOP_INTERVAL);
		
		connect.setOnClickListener(new OnClickListener(){
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
					udpInUse = true;
					udp Udp = new udp();
					if (msg.arg1>MinMeditation){
						Udp.udpSend(ip,port,"Mforward");
						lastCommandMind = true;
					}
					else if (lastCommandMind){
						Udp.udpSend(ip,port,"Mstop");
						udpInUse = false;
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
		scrollview.post(new Runnable() {
				@Override
				public void run() {
					//auto scrolls every new messages
					scrollview.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});

		try{
			scrollview.fullScroll(View.FOCUS_DOWN);
		}catch(Exception e){}
        }
    };
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.layout.menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
			case R.id.settings:
			    setContentView(R.layout.settings);
				
				Button save = (Button) findViewById(R.id.save);
				final EditText iptext = (EditText) findViewById(R.id.editIp);
				final EditText meditationtext = (EditText) findViewById(R.id.editMeditation);
				final EditText porttext = (EditText) findViewById(R.id.editPort);
				
				inMain = false;
				iptext.setText(ip);
				meditationtext.setText(Integer.toString(MinMeditation));
				porttext.setText(Integer.toString(port));
				
				save.setOnClickListener(new OnClickListener(){
					public void onClick(View v){
						Saver saver = new Saver();
						saver.writeToFile(getApplicationContext(),iptext.getText()+"\n"+porttext.getText()+"\n"+meditationtext.getText());
						Intent xi = new Intent(getApplicationContext(), robotremote.class);
						xi.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(xi);
					}
				});
				return true;

			default:
			    return false;
		}
	}

	@Override
	public void onBackPressed()
	{
		if (inMain){
			finish();
		}
		else{
		    Intent xi = new Intent(getApplicationContext(), robotremote.class);
		    xi.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    startActivity(xi);
			
			inMain = true;
		}

		super.onBackPressed();
	}

	@Override
	protected void onPause()
	{
		autorefresh.cancel();//stop reloading sensors when not used
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		// TODO: Implement this method
		autorefresh.start();
		super.onResume();
	}
}
