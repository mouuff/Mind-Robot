package com.mou.robotremote;


import android.content.*;
import java.io.*;

public class Saver
{
	public void writeToFile(Context context, String data) {
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("settings.txt", Context.MODE_PRIVATE|Context.MODE_WORLD_READABLE));
			outputStreamWriter.write(data);
			outputStreamWriter.close();
		}
		catch (IOException e) {} 
	}

	public String readFromFile(Context context) {
		try {
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(
																context.openFileInput("settings.txt")));
			String inputString;
			StringBuffer stringBuffer = new StringBuffer();                
			while ((inputString = inputReader.readLine()) != null) {
				stringBuffer.append(inputString + "\n");
			}
			return stringBuffer.toString();
		} catch (IOException e) {}
		return "";
	}
	
}
