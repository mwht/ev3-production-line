package com.transition;

import java.util.HashMap;
import java.util.Map;

import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.TextLCD;

public class Display {
	
	private TextLCD lcd;
	private Map<String, Object> properties;
	
	public Display(EV3 ev3) {
		lcd = ev3.getTextLCD();
		properties = new HashMap<String, Object>();
	}
	
	private void update() {
		int y = 0;
		lcd.clear();
		for(Map.Entry<String, Object> property: properties.entrySet()) {
			lcd.drawString(property.getKey() + ": " + property.getValue(), 0, y);
			y++;
		}
	}
	
	public synchronized void updateProperty(String propertyName, Object propertyValue) {
		properties.put(propertyName, propertyValue);
		update();
	}
}
