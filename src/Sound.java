package koro;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.*;

class Sound {
		  public long responseTime;
		  public final boolean isAdaptive;
		  public final int numOfPackets;
		  private byte[] sound_bytes;
		  private byte[] subs_bytes;
		  private byte[] m_bytes;
		  private byte[] b_bytes;

		  public Sound(boolean isAdaptive, int numOfPackets) {
		    this.isAdaptive = isAdaptive;
		    this.numOfPackets = numOfPackets;
		    sound_bytes = new byte[256 * numOfPackets * (isAdaptive ? 2 : 1)];
		    subs_bytes = new byte[256 * numOfPackets * 1];
		    m_bytes = new byte[numOfPackets];
		    b_bytes = new byte[numOfPackets];
		  }

		  public void addPakcet(byte[] buffer, int p) {
		    int m = 0;
		    int b = 2;
		    int leftCompressedByte = 0;
		    int rightCompressedByte = 0;
		    int leftByte = 0;
		    int rightByte = 0;
		    int start = 0;
		    if (isAdaptive) {
		      byte[] mm = new byte[4];
		      byte sign = (byte) ((buffer[1] & 0x80) != 0 ? 0xff : 0x00);
		      mm[3] = sign;
		      mm[2] = sign;
		      mm[1] = buffer[1];
		      mm[0] = buffer[0];
		      m = ByteBuffer.wrap(mm).order(ByteOrder.LITTLE_ENDIAN).getInt();

		      sign = (byte) ((buffer[3] & 0x80) != 0 ? 0xff : 0x00);
		      byte[] bb = new byte[4];
		      bb[3] = sign;
		      bb[2] = sign;
		      bb[1] = buffer[3];
		      bb[0] = buffer[2];
		      b = ByteBuffer.wrap(bb).order(ByteOrder.LITTLE_ENDIAN).getInt();

		      m_bytes[p] = (byte) m;
		      b_bytes[p] = (byte) b;

		      start = 4;
		      int ll;
		      int rr;
		      int song_pos;
		      int prev = 0;
		      for (int i = 0; i <= 127; i++) {
		        int pair = (int) buffer[i + start];
		        song_pos = 2 * (i * 2 + p * 256);

		        leftCompressedByte = (pair >>> 4) & 15;
		        rightCompressedByte = pair & 15;
		        leftByte = (leftCompressedByte - 8);
		        rightByte = (rightCompressedByte - 8);
		        subs_bytes[(song_pos / 2)] = (byte) leftByte;
		        subs_bytes[(song_pos / 2) + 1] = (byte) rightByte;
		        leftByte *= b;
		        rightByte *= b;

		        ll = prev + leftByte + m;
		        prev = rightByte;
		        rr = leftByte + rightByte + m;

		        sound_bytes[song_pos + 0] = (byte) (ll & 0x000000FF);
		        sound_bytes[song_pos + 1] = (byte) ((ll & 0x0000FF00) >> 8);
		        sound_bytes[song_pos + 2] = (byte) (rr & 0x000000FF);
		        sound_bytes[song_pos + 3] = (byte) ((rr & 0x0000FF00) >> 8);
		      }

		    } else {

		      for (int i = 0; i <= 127; i++) {
		        int pair = (int) buffer[i + start];
		        leftCompressedByte = (pair >>> 4) & 15;
		        rightCompressedByte = pair & 15;
		        leftByte = (leftCompressedByte - 8);
		        rightByte = (rightCompressedByte - 8);
		        int song_pos = i * 2 + p * 256;
		        subs_bytes[song_pos] = (byte) leftByte;
		        subs_bytes[song_pos + 1] = (byte) leftByte;
		        leftByte *= b;
		        rightByte *= b;
		        if (i == 0)
		          sound_bytes[i] = (byte) leftByte;
		        else
		          sound_bytes[song_pos] = (byte) (leftByte + (int) sound_bytes[song_pos - 1]);
		        sound_bytes[song_pos + 1] = (byte) (rightByte + (int) sound_bytes[song_pos]);
		      }

		    }
		  }

		  public void play() {
		    AudioFormat af = new AudioFormat(8000, (isAdaptive ? 16 : 8), 1, true, false);
		    try {
		      SourceDataLine player = AudioSystem.getSourceDataLine(af);
		      ITHAKI.ithakiPrint("Start playing sound");
		      try {
		        player.open(af, 32000);
		        player.start();
		        player.write(sound_bytes, 0, sound_bytes.length);
		        player.stop();
		        player.close();
		        ITHAKI.ithakiPrint("Stop playing sound");
		      } catch (LineUnavailableException e) {
		        ITHAKI.errorPrint("Could not open player");
		        e.printStackTrace();
		      }
		    } catch (LineUnavailableException e) {
		      ITHAKI.errorPrint("Could not create player");
		      e.printStackTrace();
		    }
		  }
		  
		  public void writeToFile(String filename) {
		    if (isAdaptive)
		      filename += "_AQ-DPCM";
		    else
		      filename += "_DPCM";
		    _writeToFile(filename, "Samples", sound_bytes);
		    _writeToFile(filename, "Subs", subs_bytes);
		    if (isAdaptive) {
		      _writeToFile(filename, "M", m_bytes);
		      _writeToFile(filename, "B", b_bytes);
		    }
		  }

		  private void _writeToFile(String filename, String type, byte[] array) {
		    File file = new File(filename + "_" + type);
		    try {
		      FileWriter fw = new FileWriter(file, false);
		      fw.write("");
		      for (byte b : array) {
		        fw.append(String.valueOf(b) + "\n");
		      }
		      fw.close();
		      ITHAKI.ithakiPrint("Sound " + type + " exported to " + filename + "_" + type);
		    } catch (IOException e) {
		      ITHAKI.errorPrint("Could not write to file: " + filename + "_" + type);
		      e.printStackTrace();
		    }
		  }

		  @Override
		  public String toString() {
		    return "Sound bytes length= " + String.valueOf(sound_bytes.length) + ", responseTime= "
		        + String.valueOf(responseTime);
		  }
		}
