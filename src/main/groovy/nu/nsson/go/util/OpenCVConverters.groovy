package nu.nsson.go.util

import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.highgui.Highgui
import java.io.ByteArrayInputStream
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class OpenCVConverters {
	
	static BufferedImage matToBufferedImage(Mat image) {
		MatOfByte matOfByte = new MatOfByte()
		Highgui.imencode(".jpg", image, matOfByte)
		
		try {
			ByteArrayInputStream input = new ByteArrayInputStream(matOfByte.toArray())
			BufferedImage resultingImage = ImageIO.read(input)
			return resultingImage
		}
		catch(Throwable t) {
			return null;
		}
	}
}
