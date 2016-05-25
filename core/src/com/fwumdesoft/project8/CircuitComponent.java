package com.fwumdesoft.project8;

public class CircuitComponent{
	public enum Type {WIRE, BATTERY, RESISTOR };
	public boolean isLamp;
	public boolean isChangeable;
	Type type;
	public double voltageDif, current, resistance;
	
	public CircuitComponent(){
		isChangeable = false;
		isLamp = false;
		voltageDif = current = resistance = 0;
	}
}
