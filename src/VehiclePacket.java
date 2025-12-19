package koro;

class VehiclePacket {
		  public int engine_run_time;
		  public int intake_air_temperature;
		  public float throttle_position;
		  public float engine_rpm;
		  public int vehicle_speed;
		  public int coolant_temperature;

		  public VehiclePacket() {
		  }

		  public void setIntake_air_temperature(String string) {
		    this.intake_air_temperature = Integer.parseInt(string, 16) - 40;
		  }

		  public void setThrottle_position(String string) {
		    this.throttle_position = Integer.parseInt(string, 16) * 100 / 255;
		  }

		  public void setVehicle_speed(String string) {
		    this.vehicle_speed = Integer.parseInt(string, 16);
		  }

		  public void setCoolant_temperature(String string) {
		    this.coolant_temperature = Integer.parseInt(string, 16) - 40;
		  }

		  public void setEngine_run_time(String string) {
		    String[] s = string.split(" ");
		    int xx = Integer.parseInt(s[0], 16);
		    int yy = Integer.parseInt(s[1], 16);
		    this.engine_run_time = (256 * xx) + yy;
		  }

		  public void setEngine_rpm(String string) {
		    String[] s = string.split(" ");
		    int xx = Integer.parseInt(s[0], 16);
		    int yy = Integer.parseInt(s[1], 16);
		    this.engine_rpm = ((xx * 256) + yy) / 4;
		  }

		  @Override
		  public String toString() {
		    return "VehiclePacket: engine_run_time=" + engine_rpm + ", intake_air_temperature=" + intake_air_temperature
		        + ", throttle_position=" + throttle_position + ", engine_rpm=" + engine_rpm + ", vehicle_speed=" + vehicle_speed
		        + ", coolant_temperature=" + coolant_temperature;
		  }

		  public String toJson() {
		    return "{\"engine_run_time\":" + engine_rpm + ", \"intake_air_temperature\":" + intake_air_temperature
		        + ", \"throttle_position\":" + throttle_position + ", \"engine_rpm\":" + engine_rpm + ", \"vehicle_speed\":"
		        + vehicle_speed + ", \"coolant_temperature\":" + coolant_temperature + "}";
		  }
		}
