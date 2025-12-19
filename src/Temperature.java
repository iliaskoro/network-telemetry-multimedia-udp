package koro;

class Temperature {
	  public final int sensor;
	  public final String temperature;

	  public Temperature(int sensor, String temp) {
	    this.sensor = sensor;
	    this.temperature = temp;
	  }

	  @Override
	  public String toString() {
	    return "Sensor " + Integer.toString(sensor) + ": " + temperature + "Â°C";
	  }
	}

