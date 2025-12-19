package koro;
import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.*;

class ITHAKI {
	  private int server_port;
	  private int client_port;
	  private InetAddress server_address;
	  private byte[] echo_code;
	  private byte[] image_code;
	  private byte[] sound_code;
	  private byte[] vehicle_code;
	  private DatagramSocket sendSocket;
	  private DatagramSocket recieveSocket;
	  private DatagramSocket copterSocket;
	  
	 public enum CAMERAS {
		  FIX, PTZ
		};

	  public ITHAKI(int server_port, int client_port, String echo_code, String image_code, String sound_code,
	      String vehicle_code) {
	    this.server_port = server_port;
	    this.client_port = client_port;
	    this.echo_code = echo_code.getBytes();
	    this.image_code = (image_code + " UDP=1024").getBytes();
	    this.sound_code = sound_code.getBytes();
	    this.vehicle_code = vehicle_code.getBytes();
	    setup();
	    ithakiPrint("initialised");
	  }

	  private void setup() {
	    byte[] ithaki_ip = { (byte) 155, (byte) 207, (byte) 18, (byte) 208 };
	    try {
	      InetAddress ithaki_address = InetAddress.getByAddress(ithaki_ip);
	      server_address = ithaki_address;
	    } catch (UnknownHostException e) {
	      errorPrint("Error geting ithaki's address from the given ip");
	      e.printStackTrace();
	      System.exit(1);
	    }
	    try {
	      sendSocket = new DatagramSocket();
	    } catch (SocketException e) {
	      errorPrint("Error creating sendSocket");
	      e.printStackTrace();
	    }
	    try {
	      recieveSocket = new DatagramSocket(client_port);
	      recieveSocket.setSoTimeout(5000);
	    } catch (SocketException e) {
	      errorPrint("Error creating recieveSocket");
	      e.printStackTrace();
	    }
	    try {
	      copterSocket = new DatagramSocket(48078);
	      copterSocket.setSoTimeout(5000);
	    } catch (SocketException e) {
	      errorPrint("Error creating recieveSocket");
	      e.printStackTrace();
	    }
	  }

	  public Packet getPacket(boolean withDelay, int sensor){
	    byte[] code = echo_code;
	    if (!withDelay)
	      code = new String("E0000").getBytes();
	    if (sensor >= 0 && sensor <= 8) {
	      code = new String(new String(code) + "T0" + Integer.toString(sensor)).getBytes();
	    }
	    DatagramPacket sendPacket = new DatagramPacket(code, code.length, server_address, server_port);
	    try {
	      ithakiPrint(new String(code) + " sent");
	      sendSocket.send(sendPacket);
	    } catch (IOException e) {
	      errorPrint("Could not send echo packet");
	      return null;
	    }
	    byte[] buffer = new byte[2048];
	    DatagramPacket recievePacket = new DatagramPacket(buffer, buffer.length);
	    long startTime = System.currentTimeMillis();
	    while (true) {
	      try {
	        recieveSocket.receive(recievePacket);
	        break;
	      } catch (SocketTimeoutException e) {
	        errorPrint("Timeout");
	        ithakiPrint("Pausing for 5sec");
	        try {
	          Thread.sleep(5000);
	        } catch (InterruptedException e1) {
	          e1.printStackTrace();
	        }
	        return null;
	      } catch (IOException e) {
	        errorPrint("Could not get echo packet");
	        e.printStackTrace();
	        System.exit(1);
	      }
	    }
	    long elapsedTime = System.currentTimeMillis() - startTime;
	    Packet packet = new Packet(new String(buffer, 0, recievePacket.getLength()), elapsedTime);
	    return packet;
	  }

	  public Image getImage(CAMERAS camera) {
	    byte[] code = image_code;
	    switch (camera) {
	      case FIX:
	        break;
	      case PTZ:
	        code = new String(new String(image_code) + " CAM=PTZ").getBytes();
	        break;
	      default:
	        break;
	    }
	    ArrayList<Byte> imageBytes = new ArrayList<Byte>();
	    long startTime = System.currentTimeMillis();
	    DatagramPacket sendPacket = new DatagramPacket(code, code.length, server_address, server_port);
	    try {
	      ithakiPrint(new String(code) + " sent");
	      sendSocket.send(sendPacket);
	    } catch (IOException e) {
	      errorPrint("Could not send image packet");
	      e.printStackTrace();
	    }
	    byte[] buffer = new byte[1024];
	    byte ff = Integer.decode("0XFF").byteValue();
	    byte d9 = Integer.decode("0XD9").byteValue();
	    byte d8 = Integer.decode("0XD8").byteValue();
	    DatagramPacket recievePacket = new DatagramPacket(buffer, buffer.length);
	    boolean image_recieved = false;
	    boolean start_writing = false;
	    while (true) {
	      try {
	        recieveSocket.receive(recievePacket);
	        for (int i = 0; i < recievePacket.getLength(); i++) {
	          if (start_writing)
	            imageBytes.add(buffer[i]);
	          if (!start_writing && i != 0 && buffer[i] == d8 && buffer[i - 1] == ff) {
	            imageBytes.add(ff);
	            imageBytes.add(d8);
	            start_writing = true;
	          }
	          if (i == recievePacket.getLength() - 1 && buffer[i] == d9 && buffer[i - 1] == ff) {
	            image_recieved = true;
	            break;
	          }
	        }
	        if (image_recieved)
	          break;
	      } catch (SocketTimeoutException e) {
	        errorPrint("Timeout");
	        ithakiPrint("Pausing for 5sec");
	        try {
	          Thread.sleep(5000);
	        } catch (InterruptedException e1) {
	          e1.printStackTrace();
	        }
	        return getImage(camera);
	      } catch (IOException e) {
	        errorPrint("Could not get image packet");
	        e.printStackTrace();
	        System.exit(1);
	      }
	    }
	    ithakiPrint("Image downloaded!");
	    long elapsedTime = System.currentTimeMillis() - startTime;
	    return new Image(imageBytes, elapsedTime);
	  }

	  public Sound getSound(int numOfPackets, int sound_type, boolean adaptive) {
	    if (adaptive && sound_type == 0) {
	      ithakiPrint("Ithaki can't get AQ-DPCM from frequency generator.");
	      ithakiPrint("If you want to get AQ-DPCM sound select a song from 1 to 99");
	      ithakiPrint("Changing to DPCM to continue");
	      adaptive = false;
	    }
	    long startTime = System.currentTimeMillis();
	    String Y = "T";
	    String L = "";
	    String AQ = "";
	    if (adaptive)
	      AQ = "AQ";
	    if (sound_type >= 1 && sound_type <= 99) {
	      Y = "F";
	      L = "L";
	      if (sound_type <= 9)
	        L += "0";
	      L += String.valueOf(sound_type);
	    }
	    byte[] code = new String(new String(sound_code) + L + AQ + Y + String.valueOf(numOfPackets)).getBytes();
	    DatagramPacket sendPacket = new DatagramPacket(code, code.length, server_address, server_port);
	    try {
	      ithakiPrint(new String(code) + " sent");
	      sendSocket.send(sendPacket);
	    } catch (IOException e) {
	      errorPrint("Could not send sound packet");
	      e.printStackTrace();
	    }
	    byte[] buffer = new byte[132];
	    DatagramPacket recievePacket = new DatagramPacket(buffer, buffer.length);

	    Sound sound = new Sound(adaptive, numOfPackets);
	    ithakiPrint("Start getting sound packets");
	    int timeouts = 0;
	    for (int p = 0; p < numOfPackets; p++) {
	      if (p % 100 == 0) {
	        ithakiPrint(String.valueOf(p) + " packets of " + String.valueOf(numOfPackets) + " received");
	      }
	      try {
	        recieveSocket.receive(recievePacket);
	        sound.addPakcet(buffer, p);
	      } catch (SocketTimeoutException e) {
	        errorPrint("Timeout on packet num: #" + Integer.toString(p));
	        timeouts += 1;
	        if (timeouts > 3) {
	          ithakiPrint("stop getting packets after 4 timeouts");
	          break;
	        }
	      } catch (IOException e) {
	        errorPrint("Could not get sound packet");
	        e.printStackTrace();
	        System.exit(1);
	      }
	    }
	    long elapsedTime = System.currentTimeMillis() - startTime;
	    sound.responseTime = elapsedTime;
	    return sound;
	  }

	  public IthakiCopterPacket getTelemetry() {
	    byte[] buffer = new byte[2048];
	    DatagramPacket recievePacket = new DatagramPacket(buffer, buffer.length);
	    int timeouts = 0;
	    long startTime = System.currentTimeMillis();
	    while (true) {
	      try {
	        copterSocket.receive(recievePacket);
	        break;
	      } catch (SocketTimeoutException e) {
	        errorPrint("Timeout");
	        timeouts += 1;
	        if (timeouts == 4) {
	          ithakiPrint("Stop trying to get packets from ithakicopter");
	          ithakiPrint("Did you open ithakicopter's jar file???");
	          ithakiPrint("Also ithakicopter send packets to port 48078");
	        }
	      } catch (IOException e) {
	        errorPrint("Could not get echo packet");
	        e.printStackTrace();
	        System.exit(1);
	      }
	    }
	    long elapsedTime = System.currentTimeMillis() - startTime;
	    IthakiCopterPacket packet = new IthakiCopterPacket(new String(buffer, 0, recievePacket.getLength()), elapsedTime);
	    return packet;
	  }

	  public VehiclePacket getVehiclePacket() {
	    byte[] code = echo_code;
	    String[] pid = { "1F", "0F", "11", "0C", "0D", "05" };
	    VehiclePacket vehiclePacket = new VehiclePacket();
	    for (int i = 0; i < pid.length; i++) {
	      code = new String(new String(vehicle_code) + "OBD=01 " + pid[i] + "\r").getBytes();
	      DatagramPacket sendPacket = new DatagramPacket(code, code.length, server_address, server_port);
	      try {
	        sendSocket.send(sendPacket);
	      } catch (IOException e) {
	        errorPrint("Could not send echo packet");
	        e.printStackTrace();
	      }
	      byte[] buffer = new byte[2048];
	      DatagramPacket recievePacket = new DatagramPacket(buffer, buffer.length);
	      while (true) {
	        try {
	          recieveSocket.receive(recievePacket);
	          String data = new String(buffer, 0, recievePacket.getLength()).substring(6);
	          switch (pid[i]) {
	            case "1F":
	              vehiclePacket.setEngine_run_time(data);
	              break;
	            case "0F":
	              vehiclePacket.setIntake_air_temperature(data);
	              break;
	            case "11":
	              vehiclePacket.setThrottle_position(data);
	              break;
	            case "0C":
	              vehiclePacket.setEngine_rpm(data);
	              break;
	            case "0D":
	              vehiclePacket.setVehicle_speed(data);
	              break;
	            case "05":
	              vehiclePacket.setCoolant_temperature(data);
	              break;
	            default:
	              errorPrint("zhe shi shenme?");
	              break;
	          }
	          break;
	        } catch (SocketTimeoutException e) {
	          errorPrint("Timeout on " + new String(code).replaceAll("\r", ""));
	          ithakiPrint("Going to ask Ithaki for the same packet");
	          ithakiPrint("Pausing for 5sec");
	          try {
	            Thread.sleep(5000);
	          } catch (InterruptedException e1) {
	            e1.printStackTrace();
	          }
	          i -= 1;
	          break;
	        } catch (IOException e) {
	          errorPrint("Could not get echo packet");
	          e.printStackTrace();
	          System.exit(1);
	        }
	      }
	    }
	    return vehiclePacket;
	  }

	  static void errorPrint(String error) {
	    System.out.println("ITHAKI: -_- " + error + " -_-");
	  }

	  public static void ithakiPrint(String str) {
	    System.out.println("ITHAKI: " + str);
	  }

	}
