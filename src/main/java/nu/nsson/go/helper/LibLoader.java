package nu.nsson.go.helper;

 import java.io.*;

 public class LibLoader {
  public static void load() {
    LibLoader.load("/opt/local/share/OpenCV/java/libopencv_java245.dylib");
  }
  
  public static void load(String path) {
	    System.load(new File(path).getAbsolutePath());
	  }
}