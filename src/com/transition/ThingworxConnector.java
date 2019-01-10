package com.transition;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ThingworxConnector {
	
	private static ThingworxConnector instance = null;
	private static Thread readerThread;
	private static ReaderThreadRunnable readerThreadRunnable;
	private static boolean running = false;
	private OnWorkingStateChanged onWorkingStateChanged;
	
	private String getDataFromURL(String urlStr) {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
									
			InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String line;
			StringBuffer sb = new StringBuffer();
			if((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (Exception e) { return ""; }
	}
	
	public void setOnWorkingStateChanged(OnWorkingStateChanged onWorkingStateChanged) {
		this.onWorkingStateChanged = onWorkingStateChanged;
	}
	
	public void startReader() {
		readerThreadRunnable = new ReaderThreadRunnable(onWorkingStateChanged);
		readerThread = new Thread(readerThreadRunnable);
		readerThread.start();
	}
	
	public void stopReader() {
		readerThreadRunnable.stop();
		try {
			readerThread.join(1000);
			readerThread.interrupt();
		} catch (Exception ignored) {}
	}
	
	public void reportError() {
		try {
			getDataFromURL("http://192.168.137.1:8000/invalidBlock");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void reportNewBlock(int color) {
		try {
			getDataFromURL("http://192.168.137.1:8000/reportBlock/"+color);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ThingworxConnector getInstance() {
		if(instance == null) instance = new ThingworxConnector();
		return instance;
	}
	
	public boolean getRunningState() {
		return readerThreadRunnable.getCurrentState();
	}

	public interface OnWorkingStateChanged {
		public void onWorkingStateChanged(boolean newState);
	}
	
	public void reportStart() {
		try {
			getDataFromURL("http://192.168.137.1:8000/start");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void reportStop() {
		try {
			getDataFromURL("http://192.168.137.1:8000/stop");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class ReaderThreadRunnable implements Runnable {
		
		private boolean lastState = false;
		private boolean currentState = false;
		private OnWorkingStateChanged onWorkingStateChanged;
		private boolean running = true;
		private boolean shouldBeOnline = true;
		
		public ReaderThreadRunnable(OnWorkingStateChanged onWorkingStateChanged) {
			this.onWorkingStateChanged = onWorkingStateChanged;
		}
		
		public boolean getCurrentState() {
			return currentState;
		}
		
		public void stop() {
			running = false;
		}
		
		@Override
		public void run() {
			String result;
			try {
				while(running) {
					result = getDataFromURL("http://192.168.137.1:8000/isWorking");
					currentState = result.equals("True");
					if(currentState != lastState) {
						onWorkingStateChanged.onWorkingStateChanged(currentState);
					}
					lastState = currentState;
					Thread.sleep(500);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
