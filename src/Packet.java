package koro;

class Packet {
	  public final String packetString;
	  public final long responseTime;
	  public final boolean hasTempInfo;
	  public final Temperature temperature;

	  public Packet(String packetString, long responseTime) {
	    this.packetString = packetString;
	    this.responseTime = responseTime;
	    String[] splitted = packetString.split(" ");
	    if (splitted[3].charAt(0) == 'T') {
	      hasTempInfo = true;
	      String sensor = splitted[3].substring(1);
	      String temp = splitted[6];
	      temperature = new Temperature(Integer.valueOf(sensor), temp);
	    } else {
	      hasTempInfo = false;
	      temperature = null;
	    }
	  }

	  @Override
	  public String toString() {
	    if (hasTempInfo)
	      return packetString + " with response time = " + responseTime + "ms | " + temperature.toString();
	    else
	      return packetString + " with response time = " + responseTime + "ms";
	  }
	}
