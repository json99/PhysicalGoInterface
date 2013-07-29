package nu.nsson.go.engine

import java.awt.image.BufferedImage;

import net.sf.gogui.gtp.GtpClient.IOCallback
import nu.nsson.go.Main;
import nu.nsson.go.camera.WebCamWrapper
import nu.nsson.go.camera.WebCamWrapperUpdate
import nu.nsson.go.gnugo.GnuGoWrapper
import nu.nsson.go.util.OpenCVConverters

import org.opencv.core.CvType;
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Core
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.highgui.Highgui

class PhysicalGoInterfaceEngine implements WebCamWrapperUpdate, IOCallback {
	
	private GoEngineGuiUpdate delegate
	
	def webCam
	def gnuGo
	
	def currentImage
	
	def lastCommandWasShowBoard
	
	public PhysicalGoInterfaceEngine() {
		// Initialize WebCam
		webCam = WebCamWrapper.getInstance()
		webCam.delegate = this
		
		// Initialize Gnu Go
		gnuGo = new GnuGoWrapper(this)
		
		lastCommandWasShowBoard = false
	}
	
	def start() {	
		// Show initial board
		lastCommandWasShowBoard = true
		gnuGo.showBoard()
	}
	
	def stop() {
		webCam.stop()
	}
	
	def startCamera() {
		webCam.start()
	}
	
	def stopCamera() {
		webCam.stop()	
	}
	
	public void setDelegate(GoEngineGuiUpdate d) {
		delegate = d
	}
	
	public GoEngineGuiUpdate getDelegate() {
		return delegate
	}

	public void newImage(Mat img) {
		currentImage = img
		//currentImage = processImage(currentImage)
		
		def buffImg = OpenCVConverters.matToBufferedImage(currentImage)
		if(delegate) {
			delegate.webCamBoardUpdate(buffImg)
		}
	}

	public void receivedInvalidResponse(String msg) {
		println "Invalid: ${msg}"
	}
	public void receivedResponse(boolean arg0, String msg) {
		println "Response: ${msg}"
		
		if(delegate) {
			if(lastCommandWasShowBoard) {
				lastCommandWasShowBoard = false
				delegate.computerBoardUpdate("<html><pre>${msg}</pre></html>")
			}
			else {
				lastCommandWasShowBoard = true
				gnuGo.showBoard()
				
				delegate.computerMoveUpdate("<html><h1>${msg}</h1></html>")
			}
		}
	}
	public void receivedStdErr(String msg) {
		println "StdErr: ${msg}"
	}
	public void sentCommand(String cmd) {
		println "Cmd: ${cmd}"
	}
	
	def generateBlackMove() {
		gnuGo.generateBlackMove()
	}
	
	def generateWhiteMove() {
		gnuGo.generateWhiteMove()
	}
	
	def processImage(img) {
		
		
		def imgRes = new Mat()
		imgRes = img
		Imgproc.cvtColor(imgRes, imgRes, Imgproc.COLOR_BGR2GRAY)
		
		//Imgproc.medianBlur(img, imgRes, 3)
		//Imgproc.GaussianBlur(imgRes, imgRes, new Size(7, 7), 1, 1)
		
		def edges = new Mat()
		Imgproc.Canny(imgRes, edges, 80, 120)
		def lines = new Mat()
		Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 2, 2, 100, 1)
		
		Imgproc.cvtColor(imgRes, imgRes, Imgproc.COLOR_GRAY2BGR)
		for(int i = 0; i < lines.cols(); i++) {
			def points = lines.get(0, i)
			
			Core.line(imgRes, new Point(points[0], points[1]), new Point(points[2], points[3]), new Scalar(255, 0, 0), 3)
		}
		
		return imgRes
	}
	
	def updateGui() {
		def buffImg = OpenCVConverters.matToBufferedImage(currentImage)
		if(delegate) {
			delegate.webCamBoardUpdate(buffImg)
		}
	}
	
	def saveImage() {
		Highgui.imwrite("savedImage.jpg", currentImage)
	}
	
	def loadImage() {
		currentImage = Highgui.imread("savedImage.jpg")
		
		updateGui()
	}
	
	def convertToGrey() {
		Imgproc.cvtColor(currentImage, currentImage, Imgproc.COLOR_BGR2GRAY)
		
		updateGui()
	}
	
	def addGaussianBlur() {
		Imgproc.GaussianBlur(currentImage, currentImage, new Size(11, 11), 0)
		
		updateGui()
	}
	
	def addAdaptiveTresholdFilter() {
		Imgproc.adaptiveThreshold(currentImage, currentImage, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2)
		Core.bitwise_not(currentImage, currentImage)
		
		updateGui()
	}
	
	def addDilate() {
		def kernel = new Mat(3,3, CvType.CV_8U, Scalar.all(0))
		kernel.put(0, 1, [1] as byte[])
		kernel.put(1, 0, [1] as byte[])
		kernel.put(1, 1, [1] as byte[])
		kernel.put(1, 2, [1] as byte[])
		kernel.put(2, 1, [1] as byte[])
		
		Imgproc.dilate(currentImage, currentImage, kernel)
		
		updateGui()
	}
	
	def findLargestBlob() {
		def max = -1
		def maxPt
		
		def sizeOfImage = currentImage.size()
		for(int y = 0;y < sizeOfImage.height;y++)
		{
			for(int x = 0;x < sizeOfImage.width;x++)
			{		
				def color = currentImage.get(y, x)
				if(color[0] > 128)
				{
					int area = Imgproc.floodFill(currentImage, new Mat(), new Point(x, y), new Scalar(64))
					
					if(area > max)
					{
						maxPt = new Point(x,y)
						max = area
					}
				}
			}
		}
		
		Imgproc.floodFill(currentImage, new Mat(), maxPt, new Scalar(255))
		
		for(int y = 0;y < sizeOfImage.height;y++)
		{
			for(int x = 0;x < sizeOfImage.width;x++)
			{
				def color = currentImage.get(y, x)
				if(color[0] == 64)
				{
					int area = Imgproc.floodFill(currentImage, new Mat(), new Point(x, y), new Scalar(0))
				}
			}
		}
		
		def kernel = new Mat(3,3, CvType.CV_8U, Scalar.all(0))
		kernel.put(0, 1, [1] as byte[])
		kernel.put(1, 0, [1] as byte[])
		kernel.put(1, 1, [1] as byte[])
		kernel.put(1, 2, [1] as byte[])
		kernel.put(2, 1, [1] as byte[])
		Imgproc.erode(currentImage, currentImage, kernel)
		Imgproc.erode(currentImage, currentImage, kernel)
		
		updateGui()
	}
	
	def detectLines() {
		def lines = new Mat()
		
		Imgproc.HoughLines(currentImage, lines, 1, Math.PI / 180, 200)
		
		for(int i = 0; i < lines.cols(); i++) {
			def line = lines.get(0, i)
			drawLine(line, currentImage, new Scalar(128))
		}
		
		updateGui()
	}
	
	def drawLine(line, img, rgb)
	{
		if(line[1]!=0)
		{
			float m = -1/Math.tan(line[1])
			float c = line[0]/Math.sin(line[1])
	 
			Core.line(img, new Point(0, c), new Point(img.size().width, m*img.size().width+c), rgb)
		}
		else
		{
			Core.line(img, new Point(line[0], 0), new Point(line[0], img.size().height), rgb)
		}
	}
}
