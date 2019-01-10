package com.transition;

import lejos.hardware.ev3.EV3;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;

public class Camera {
	private static final Port COLOR_SENSOR_PORT = SensorPort.S3;
	private final EV3ColorSensor colorSensor;
	private static Thread camThread;
	private static CameraThreadRunnable cameraThreadRunnable;
	private static boolean running = false;
	private Display display;
	
	private OnWrongBlockHandler onWrongBlockHandler;
	private OnBlockPassedHandler onBlockPassedHandler;
	
	public Camera(EV3 ev3, Display display) {
		colorSensor = new EV3ColorSensor(COLOR_SENSOR_PORT);
		this.display = display;
	}
	
	public void startCamera() {
		if(!running) {
			cameraThreadRunnable = new CameraThreadRunnable(display, onWrongBlockHandler, onBlockPassedHandler);
			camThread = new Thread(cameraThreadRunnable);
			camThread.start();
			running = true;
		}
	}
	
	public void stopCamera() {
		cameraThreadRunnable.stopRunnable();
		try {
			camThread.join(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private class CameraThreadRunnable implements Runnable {
		
		private Display display;
		private OnWrongBlockHandler onWrongBlockHandler;
		private OnBlockPassedHandler onBlockPassedHandler;
		private boolean running = true;
		private boolean blockAlreadySeen = false;
		
		public CameraThreadRunnable(Display display, OnWrongBlockHandler onWrongBlockHandler, OnBlockPassedHandler onBlockPassedHandler) {
			this.display = display;
			this.onWrongBlockHandler = onWrongBlockHandler;
			this.onBlockPassedHandler = onBlockPassedHandler;
		}
		
		public void stopRunnable() {
			running = false;
		}
		
		@Override
		public void run() {
			while(running) {
				try {
					SensorMode sensorColorID = colorSensor.getColorIDMode();
					float[] sample = new float[sensorColorID.sampleSize()]; // ugh.
					sensorColorID.fetchSample(sample, 0);
					display.updateProperty("Color ID", sample[0]);

					if(sample[0] > -0.01 && sample[0] < 0.01) {
						//audio.playTone(1300, 50);
						if(!blockAlreadySeen)
							onWrongBlockHandler.onWrongBlock();
						blockAlreadySeen = true;
					} else if(sample[0] > 0.99){
						if(!blockAlreadySeen)
							onBlockPassedHandler.onBlockPassed((int) sample[0]);
						blockAlreadySeen = true;
					} else {
						blockAlreadySeen = false;
					}
					
					Thread.sleep(100);
				} catch (Exception ignored) {}
			}
		}
	}
	
	public void setOnWrongBlockHandler(OnWrongBlockHandler onWrongBlockHandler) {
		this.onWrongBlockHandler = onWrongBlockHandler;
	}
	
	public void setOnBlockPassedHandler(OnBlockPassedHandler onBlockPassedHandler) {
		this.onBlockPassedHandler = onBlockPassedHandler;
	}
	
	public interface OnWrongBlockHandler {
		public void onWrongBlock();
	}
	
	public interface OnBlockPassedHandler {
		public void onBlockPassed(int color);
	}
}
