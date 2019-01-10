package com.transition;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.transition.ThingworxConnector.OnWorkingStateChanged;

import lejos.hardware.ev3.EV3;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;

public class ProductionLine {
	private RegulatedMotor lineMotor;
	private static final Port LINE_MOTOR_PORT = MotorPort.A;
	private boolean isLineRunning = false;
	private int speed = 165;
	private final int MAX_SPEED = 360;
	private final int MIN_SPEED = 10;
	private int stopsSinceStart = 0;
	protected final Display display;
	private boolean shouldRun = false;
	private ThingworxConnector thingworxConnector;


	public ProductionLine(EV3 ev3, Display disp) {
		lineMotor = new EV3MediumRegulatedMotor(LINE_MOTOR_PORT);
		isLineRunning = false;
		display = disp;
		thingworxConnector = new ThingworxConnector();
		thingworxConnector.setOnWorkingStateChanged(new OnWorkingStateChanged() {

			@Override
			public void onWorkingStateChanged(boolean newState) {
				shouldRun = newState;
				display.updateProperty("ShouldRun", newState);
				if(newState) startLine();
				else stopLine();
			}
			
		});
		thingworxConnector.startReader();
		lineMotor.setSpeed(speed);
	}
	
	public void startLine() {
		if(!isLineRunning && shouldRun) {
			lineMotor.forward();
			isLineRunning = true;
		}
	}
	
	public void stopLine() {
		if(isLineRunning) {
			lineMotor.stop();
			isLineRunning = false;
		}
	}
	
	public void stopLineAndReportError() {
		if(isLineRunning) {
			lineMotor.stop();
			thingworxConnector.reportError();
			display.updateProperty("Failures", ++stopsSinceStart);
			isLineRunning = false;
		}
	}
	
	public void endLine() {
		thingworxConnector.stopReader();
		stopLine();
	}
	
	public void speedUp() {
		if(isLineRunning) {
			speed += 5;
			display.updateProperty("Speed", speed);
			lineMotor.setSpeed(speed);
		}
	}
	
	public void speedDown() {
			if(isLineRunning) {
			speed -= 5;
			display.updateProperty("Speed", speed);
			lineMotor.setSpeed(speed);
		}
	}
	
	public int getSpeed() {
		return speed;
	}

	public void passCube() {
		// TODO Auto-generated method stub
		
	}
}
