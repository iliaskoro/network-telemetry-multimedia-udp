package koro;

class IthakiCopterPacket {
	  public final String packetString;
	  public final String datetime;
	  public final int lmotor;
	  public final int rmotor;
	  public final int altitude;
	  public final float temperature;
	  public final float pressure;
	  public final long responseTime;

	  public IthakiCopterPacket(String packetString, long responseTime) {
	    String[] splitted = packetString.split(" ");
	    assert (splitted[0] == "ITHAKICOPTER");
	    this.packetString = packetString;
	    this.datetime = splitted[1] + splitted[2];
	    this.lmotor = Integer.valueOf(splitted[3].substring(7));
	    this.rmotor = Integer.valueOf(splitted[4].substring(7));
	    this.altitude = Integer.valueOf(splitted[5].substring(9));
	    this.temperature = Float.valueOf(splitted[6].substring(12));
	    this.pressure = Float.valueOf(splitted[7].substring(9));
	    this.responseTime = responseTime;
	  }

	  @Override
	  public String toString() {
	    return packetString + " with response time = " + responseTime + "ms";
	  }

	  public String toJson() {
	    return "{" + "\"LMOTOR\":" + Integer.toString(lmotor) + ",\"RMOTOR\":" + Integer.toString(rmotor) + ",\"ALTITUDE\":"
	        + Integer.toString(altitude) + ",\"TEMPERATURE\":" + Float.toString(temperature) + ",\"PRESSURE\":"
	        + Float.toString(pressure) + "}";
	  }

	}
