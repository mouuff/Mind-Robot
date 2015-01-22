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
import android.view.*;
import android.content.*;
import android.webkit.*;
import android.os.*;


public class robotremote extends Activity {
	BluetoothAdapter bluetoothAdapter;
	
	TextView tv, Meditation,Attention;
	Button connect;
	ScrollView scrollview;
	
	boolean inMain = true;
	
	boolean lastCommandMind = false;
	
	TGDevice tgDevice;
	final boolean rawEnabled = false;
	
	CountDownTimer autorefresh;
	
	String ip;
	int MinMeditation, port;
	DatagramSocket s;
	InetAddress local;
	
    private void writeToFile(String data) {
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("settings.txt", Context.MODE_PRIVATE|Context.MODE_WORLD_READABLE));
			outputStreamWriter.write(data);
			outputStreamWriter.close();
		}
		catch (IOException e) {
			Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
		} 
	}

	private String readFromFile() {
		try {
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(
																openFileInput("settings.txt")));
			String inputString;
			StringBuffer stringBuffer = new StringBuffer();                
			while ((inputString = inputReader.readLine()) != null) {
				stringBuffer.append(inputString + "\n");
			}
			return stringBuffer.toString();
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
		}
		return "";
	}
	
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
	
	public boolean send(String s){
		try{
			udpSend(ip,12345,s);
			tv.append("[*] UDP send: "+s+" "+ip+"\n");
			
			try{
				scrollview.fullScroll(View.FOCUS_DOWN);
			}catch(Exception e){}
			return true;
		}catch(Exception e){
			tv.append("\n[x] UDP send error");
			return false;
		}
	}
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tv = (TextView)findViewById(R.id.log);
		connect = (Button)findViewById(R.id.connect);
        tv.append("Android version: " + Integer.valueOf(android.os.Build.VERSION.SDK) + "\n" );
		final WebView engine = (WebView) findViewById(R.id.capteurs);
		

		
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		scrollview = (ScrollView) findViewById(R.id.logScroll);
		
		
		
		String file;
		String fs[];
		file = readFromFile();
		
		if (file.equals("")){
			port = 12345;
			ip = "127.0.0.1";
			MinMeditation = 70;
			writeToFile(ip+"\n"+Integer.toString(port)+"\n"+Integer.toString(MinMeditation));
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
		
		final Button forward = (Button) findViewById(R.id.Forward);
		final Button backward = (Button) findViewById(R.id.Backward);
		final Button left = (Button) findViewById(R.id.Left);
		final Button right = (Button) findViewById(R.id.Right);
		
		final Button CUp = (Button) findViewById(R.id.CUp);
		final Button CDown = (Button) findViewById(R.id.CDown);
		final Button CLeft = (Button) findViewById(R.id.CLeft);
		final Button CRight = (Button) findViewById(R.id.CRight);
		
		Meditation = (TextView) findViewById(R.id.TVM);
		Attention = (TextView) findViewById(R.id.TVA);
		
		autorefresh = new CountDownTimer(15000,1000){
			public void onTick(long millis){
				
			}
			public void onFinish(){
				try{
					engine.loadUrl("http://"+ip);
				}catch(Exception e){}
				autorefresh.start();
			}
		};
		autorefresh.start();
		
		
		right.setOnTouchListener(new OnTouchListener(){
			public boolean onTouch(View v,MotionEvent event){
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					send("Mright");
					lastCommandMind = false;
				}
				else if (event.getAction() == MotionEvent.ACTION_UP){
					send("Mstop");
				}
				return true;
			}
		});
		left.setOnTouchListener(new OnTouchListener(){
			public boolean onTouch(View v,MotionEvent event){
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					send("Mleft");
					lastCommandMind = false;
				}
				else if (event.getAction() == MotionEvent.ACTION_UP){
					send("Mstop");
				}
				return true;
			}
		});
		forward.setOnTouchListener(new OnTouchListener(){
			public boolean onTouch(View v,MotionEvent event){
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					send("Mforward");
					lastCommandMind = false;
				}
				else if (event.getAction() == MotionEvent.ACTION_UP){
					send("Mstop");
				}
				return true;
			}
		});
		backward.setOnTouchListener(new OnTouchListener(){
			public boolean onTouch(View v,MotionEvent event){
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					send("Mbackward");
					lastCommandMind = false;
				}
				else if (event.getAction() == MotionEvent.ACTION_UP){
					send("Mstop");
				}
			return true;
			}
		});
		
		
		CRight.setOnTouchListener(new OnTouchListener(){
				public boolean onTouch(View v,MotionEvent event){
					if (event.getAction() == MotionEvent.ACTION_DOWN){
						send("Cright");
					}
					else if (event.getAction() == MotionEvent.ACTION_UP){
						send("Cstop");
					}
					return true;
				}
			});
		CLeft.setOnTouchListener(new OnTouchListener(){
				public boolean onTouch(View v,MotionEvent event){
					if (event.getAction() == MotionEvent.ACTION_DOWN){
						send("Cleft");
					}
					else if (event.getAction() == MotionEvent.ACTION_UP){
						send("Cstop");
					}
					return true;
				}
			});
		CUp.setOnTouchListener(new OnTouchListener(){
				public boolean onTouch(View v,MotionEvent event){
					if (event.getAction() == MotionEvent.ACTION_DOWN){
						send("Cup");
					}
					else if (event.getAction() == MotionEvent.ACTION_UP){
						send("Cstop");
					}
					return true;
				}
			});
		CDown.setOnTouchListener(new OnTouchListener(){
				public boolean onTouch(View v,MotionEvent event){
					if (event.getAction() == MotionEvent.ACTION_DOWN){
						send("Cdown");
					}
					else if (event.getAction() == MotionEvent.ACTION_UP){
						send("Cstop");
					}
					return true;
				}
			});
		
		
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
					
					if (msg.arg1>MinMeditation){
						send("Mforward");
						lastCommandMind = true;
					}
					else if (lastCommandMind){
						send("Mstop");
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
					scrollview.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});

		try{
			scrollview.fullScroll(View.FOCUS_DOWN);
		}catch(Exception e){}
        }
    };
    
    public void doStuff(View view) {
    	if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED){
    		tgDevice.connect(rawEnabled);
		}
    	//tgDevice.ena
    }
	
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
						writeToFile(iptext.getText()+"\n"+porttext.getText()+"\n"+meditationtext.getText());
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
}
