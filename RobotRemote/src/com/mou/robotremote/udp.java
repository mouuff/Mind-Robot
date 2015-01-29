package com.mou.robotremote;

import android.widget.*;
import java.io.*;
import java.net.*;

public class udp
{
	DatagramSocket s;
	InetAddress local;

	public void udpSend(String server,int server_port,String messageStr){
		try
		{
			s = new DatagramSocket();
		}
		catch (SocketException e)
		{
		}
		try
		{
			local = InetAddress.getByName(server);
		}
		catch (UnknownHostException e){}
		int msg_length = messageStr.length();
		byte[] message = messageStr.getBytes();
		DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);
		try
		{
			s.send(p);
		}
		catch (IOException e){}

	}
}
