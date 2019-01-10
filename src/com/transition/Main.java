package com.transition;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.transition.Camera.OnWrongBlockHandler;

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
	
	private static final int MOTOR_SPEED_STEP = 5;
	
	private static final float COLOR_THRESHOLD = 0.09f;	
	
	public static void main(String[] args) throws Exception {
		EV3 ev3 = (EV3) BrickFinder.getLocal();
		final Display display = new Display(ev3);
		Keys keys = ev3.getKeys();
		final Audio audio = ev3.getAudio();
		final ProductionLine productionLine = new ProductionLine(ev3, display);
		final Camera camera = new Camera(ev3, display);
		int keysDown = 0;
		
		audio.setVolume(20);
		
		productionLine.startLine();
		
		camera.setOnWrongBlockHandler(new OnWrongBlockHandler() {

			@Override
			public void onWrongBlock() {
				productionLine.stopLineAndReportError();
			}
			
		});
		camera.startCamera();
		
		while(keysDown != Keys.ID_ESCAPE) {
			switch(keysDown) {
				case Keys.ID_UP:
					audio.playTone(1000, 100);
					productionLine.speedUp();
					break;
				case Keys.ID_DOWN:
					audio.playTone(800, 100);
					productionLine.speedDown();
					break;
				case Keys.ID_ENTER:
					productionLine.startLine();
					break;
			}
					
			keysDown = keys.waitForAnyPress(500);
		}
		audio.setVolume(0);
		productionLine.endLine();
		camera.stopCamera();
	}
}
