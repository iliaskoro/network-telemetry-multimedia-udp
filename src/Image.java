package koro;
import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

class Image {
	  final ArrayList<Byte> imageBytes;
	  final long responseTime;

	  public Image(ArrayList<Byte> imageBytes, long responseTime) {
	    this.imageBytes = imageBytes;
	    this.responseTime = responseTime;
	  }

	  public void writeToFile(String filename) {
	    FileOutputStream os;
	    try {
	      os = new FileOutputStream(filename, false);
	      for (int i = 0; i < imageBytes.size(); i++) {
	        if (i == 0) {
	          os.write(imageBytes.get(i));
	          os = new FileOutputStream(filename, true);
	        } else
	          os.write(imageBytes.get(i));
	      }
	      os.close();
	      ITHAKI.ithakiPrint("Image exported to " + filename);
	    } catch (FileNotFoundException e) {
	      System.out.println("Could not write to file " + filename);
	      e.printStackTrace();
	    } catch (IOException e) {
	      System.out.println("Could not write to file " + filename);
	      e.printStackTrace();
	    }
	  }
	}
