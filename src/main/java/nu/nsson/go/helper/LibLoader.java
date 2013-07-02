import org.opencv.core.Core;
import java.io.*;
public class LibLoader {
  public static void load() {
    System.load(new File("/opt/local/share/OpenCV/java/libopencv_java245.dylib").getAbsolutePath());
  }
}