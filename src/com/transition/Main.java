package com.transition;

import lejos.hardware.Audio;
import lejos.hardware.BrickFinder;
import lejos.hardware.Keys;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.RegulatedMotor;


/**
 * EV3 production line Java driver.
 * 
 * @author mwht
 * @author Ashen
 *
 */
public class Main {
	
	private static final Port LINE_MOTOR_PORT = MotorPort.A;
	private static final int MOTOR_SPEED_STEP = 5;
	private static final Port COLOR_SENSOR_PORT = SensorPort.S3;
	
	private static final float COLOR_THRESHOLD = 0.09f;

	public static void main(String[] args) {
		EV3 ev3 = (EV3) BrickFinder.getLocal();
		final TextLCD lcd = ev3.getTextLCD();
		Keys keys = ev3.getKeys();
		final Audio audio = ev3.getAudio();
		final EV3ColorSensor colorSensor = new EV3ColorSensor(COLOR_SENSOR_PORT);
		final RegulatedMotor lineMotor = new EV3MediumRegulatedMotor(LINE_MOTOR_PORT);
		int keysDown = 0;
		int keysSpeed = 165;
		
		audio.setVolume(20);
		
		lineMotor.setSpeed(keysSpeed);
	
		lineMotor.forward();
		
		
		
		Thread camThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
					while(true) {
						try {
							SensorMode sensorColorID = colorSensor.getColorIDMode();
							float[] sample = new float[sensorColorID.sampleSize()]; // ugh.
							sensorColorID.fetchSample(sample, 0);
							lcd.clear();
							lcd.drawString("Color ID: " + sample[0], 0, 1);

							if(!((sample[0] > 1.99 && sample[0] < 2.01) || (sample[0] > 3.99 && sample[0] < 4.01))) {
								audio.playTone(1300, 50);
							}
							
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
			}
			
		});
		
		camThread.start();
		
		while(keysDown != Keys.ID_ESCAPE) {
			switch(keysDown) {
				case Keys.ID_UP:
					audio.playTone(1000, 100);
					keysSpeed += MOTOR_SPEED_STEP;
					break;
				case Keys.ID_DOWN:
					audio.playTone(800, 100);
					keysSpeed -= MOTOR_SPEED_STEP;
					break;
			}
			lineMotor.setSpeed(keysSpeed);
					
			//lcd.clear();
			//lcd.drawString(String.valueOf(keysSpeed), 0, 0);
			keysDown = keys.waitForAnyPress(500);
		}
		lineMotor.stop();
		try {
			camThread.stop();
		} catch(Exception ignored) {}
	}
	
}
