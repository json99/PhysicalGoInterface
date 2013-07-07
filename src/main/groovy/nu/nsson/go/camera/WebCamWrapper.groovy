package nu.nsson.go.camera

import org.opencv.highgui.VideoCapture
import org.opencv.core.Mat

class WebCamWrapper {
	
	private static WebCamWrapper instance = null
	
	public static WebCamWrapper getInstance() {
		if(instance == null) {
			instance = new WebCamWrapper()
		}
	}
	
	private VideoCapture captureDevice = null
	
	private WebCamWrapper() {
		captureDevice = new VideoCapture(0)
		
		def initialized = false
		3.times {
			if(!initialized) {
				Thread.sleep(1000)
			}
		}
		
		if(!captureDevice.isOpened()) {
			println 'Did not connect to camera'
		}
		else {
			println "Found camera ${captureDevice.toString()}"
		}
	}
	
	public Mat captureImage() {
		if(captureDevice && captureDevice.isOpened()) {
			def img = new Mat()
			captureDevice.read(img)
			
			return img
		}
	}

}
