package nu.nsson.go.camera

import org.opencv.highgui.VideoCapture
import org.opencv.core.CvType;
import org.opencv.core.Mat
import org.opencv.core.Core
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc

import nu.nsson.go.util.PhysicalGoInterfaceProperties
import nu.nsson.go.util.OpenCVConverters

import java.util.Timer
import java.util.TimerTask

import javax.swing.text.MaskFormatter.UpperCaseCharacter;

class WebCamWrapper extends TimerTask {
	
	private static WebCamWrapper instance = null
	
	public static WebCamWrapper getInstance() {
		if(instance == null) {
			instance = new WebCamWrapper()
		}
	}
	
	private VideoCapture captureDevice = null
	private WebCamWrapperUpdate delegate = null
	def updateThread
	def stopThread = false
	private Timer timer
	
	private WebCamWrapper() {
		def config = PhysicalGoInterfaceProperties.getInstance()
		captureDevice = new VideoCapture(config.getWebCamDeviceId(0))
		
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
	
	public void setDelegate(WebCamWrapperUpdate d) {
		delegate = d
	}
	
	def start() {
		timer = new Timer()
        timer.schedule(this, 0, 1000)
	}
	
	def stop() {
		timer.cancel()
	}

	@Override
	public void run() {
		if(captureDevice && captureDevice.isOpened()) {
			def img = new Mat()
			captureDevice.read(img)
			
			Core.flip(img, img, -1)
			
			if(delegate) {
				delegate.newImage(img)
			}
		}
	}
}
