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
import org.opencv.core.MatOfPoint2f
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
		
		mergeRelatedLines(lines, currentImage)
		
		for(int i = 0; i < lines.cols(); i++) {
			def line = lines.get(0, i)
			drawLine(line, currentImage, new Scalar(128))
		}
		
		currentImage = findBoardLines(lines, currentImage)
		
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
	
	def mergeRelatedLines(lines, img)
	{
		def current;
		def points
		for(int i = 0;i < lines.cols();i++)
		{
			current = lines.get(0, i)
			
			// Already used line
			if(current[0] == 0 && current[1]==-100)
				continue
	
			def p1 = current[0]
			def theta1 = current[1]
	
			def pt1current = new Point()
			def pt2current = new Point()
			if(theta1 > Math.PI * 45 / 180 && theta1 < Math.PI * 135 / 180)
			{
				pt1current.x = 0
				pt1current.y = p1 / Math.sin(theta1)
	
				pt2current.x = img.size().width
				pt2current.y = -pt2current.x / Math.tan(theta1) + p1 / Math.sin(theta1)
			}
			else
			{
				pt1current.y = 0
				pt1current.x = p1 / Math.cos(theta1)
	
				pt2current.y = img.size().height
				pt2current.x = -pt2current.y / Math.tan(theta1) + p1 / Math.cos(theta1)
			}
	
			def	pos
			for(int j = 0; j < lines.cols(); j++)
			{
				pos = lines.get(0, j)
				
				if(current == pos)
					continue
	
			
				if(Math.abs(pos[0]-current[0]) < 20 && Math.abs(pos[1]-current[1]) < Math.PI * 10 / 180)
				{
					float p = pos[0]
					float theta = pos[1]
	
					def pt1 = new Point()
					def pt2 = new Point()
					if(pos[1] > Math.PI * 45 / 180 && pos[1] < Math.PI * 135 / 180)
					{
						pt1.x = 0
						pt1.y = p / Math.sin(theta)
	
						pt2.x = img.size().width
						pt2.y = -pt2.x / Math.tan(theta) + p / Math.sin(theta)
					}
					else
					{
						pt1.y = 0
						pt1.x = p / Math.cos(theta)
	
						pt2.y = img.size().height
						pt2.x = -pt2.y / Math.tan(theta) + p / Math.cos(theta)
					}
	
					def first = ((pt1.x-pt1current.x) * (pt1.x-pt1current.x) + (pt1.y-pt1current.y) * (pt1.y-pt1current.y) < 64*64)
					def last = ((pt2.x-pt2current.x) * (pt2.x-pt2current.x) + (pt2.y-pt2current.y) * (pt2.y-pt2current.y) < 64*64)
					
					if(first && last)
					{
						current[0] = (current[0]+pos[0])/2
						current[1] = (current[1]+pos[1])/2
	
						pos[0]=0
						pos[1]=-100
					}
				}
			}
		}
	}
	
	def findBoardLines(lines, img) {
		def topEdge = new Point(100000, 100000)
		def topYIntercept = 100000
		def topXIntercept = 0
		
		def bottomEdge = new Point(-100000, -100000)
		def bottomYIntercept = 0
		def bottomXIntercept = 0
		
		def leftEdge = new Point(100000, 100000)
		def leftXIntercept = 100000
		def leftYIntercept = 0
		
		def rightEdge = new Point(-100000, -100000)
		def rightXIntercept = 0
		def rightYIntercept = 0
		
		for(int i = 0;i<lines.cols();i++)
		{
			def current = lines.get(0, i)
	
			def p = current[0]
			def theta = current[1]
	
			if(p == 0 && theta == -100)
				continue
			
			def xIntercept = p / Math.cos(theta)
			def yIntercept = p / (Math.cos(theta) * Math.sin(theta))
	
			if(theta > Math.PI * 80 / 180 && theta < Math.PI * 100 / 180)
			{
				if(p < topEdge.x)
					topEdge = new Point(current[0], current[1])
	
				if(p > bottomEdge.x)
					bottomEdge = new Point(current[0], current[1])
	
			}
			else if(theta < Math.PI * 10 / 180 || theta > Math.PI * 170 / 180)
			{
				if(xIntercept > rightXIntercept)
				{
					rightEdge = new Point(current[0], current[1])
					rightXIntercept = xIntercept
				}
				else if(xIntercept <= leftXIntercept)
				{
					leftEdge = new Point(current[0], current[1])
					leftXIntercept = xIntercept
				}
			}
		}
	
		def left1 = new Point()
		def left2 = new Point()
		def right1 = new Point()
		def right2 = new Point()
		def bottom1 = new Point()
		def bottom2 = new Point()
		def top1 = new Point()
		def top2 = new Point()
	
		int height=img.size().height
		int width=img.size().width
	
		if(leftEdge.y != 0)
		{
			left1.x = 0		
			left1.y = leftEdge.x / Math.sin(leftEdge.y)
			left2.x = width
			left2.y = -left2.x / Math.tan(leftEdge.y) + left1.y
		}
		else
		{
			left1.y = 0
			left1.x = leftEdge.x / Math.cos(leftEdge.y)
			left2.y = height
			left2.x = left1.x - height * Math.tan(leftEdge.y)
		}
	
		if(rightEdge.y != 0)
		{
			right1.x = 0
			right1.y = rightEdge.x / Math.sin(rightEdge.y)
			right2.x = width
			right2.y = -right2.x / Math.tan(rightEdge.y) + right1.y
		}
		else
		{
			right1.y = 0
			right1.x = rightEdge.x / Math.cos(rightEdge.y)
			right2.y = height
			right2.x = right1.x - height * Math.tan(rightEdge.y)
		}
	
		bottom1.x = 0
		bottom1.y = bottomEdge.x / Math.sin(bottomEdge.y)
		bottom2.x = width
		bottom2.y = -bottom2.x / Math.tan(bottomEdge.y) + bottom1.y
	
		top1.x = 0
		top1.y = topEdge.x / Math.sin(topEdge.y)
		top2.x = width
		top2.y = -top2.x / Math.tan(topEdge.y) + top1.y
	
		// Next, we find the intersection of these four lines
		double leftA = left2.y - left1.y
		double leftB = left1.x - left2.x
		double leftC = leftA * left1.x + leftB * left1.y
	 
		double rightA = right2.y - right1.y
		double rightB = right1.x - right2.x
		double rightC = rightA * right1.x + rightB * right1.y
	 
		double topA = top2.y - top1.y
		double topB = top1.x - top2.x
		double topC = topA * top1.x + topB * top1.y
	 
		double bottomA = bottom2.y - bottom1.y
		double bottomB = bottom1.x - bottom2.x
		double bottomC = bottomA * bottom1.x + bottomB * bottom1.y
	 
		// Intersection of left and top
		double detTopLeft = leftA * topB - leftB * topA
		def ptTopLeft = new Point((topB * leftC - leftB * topC) / detTopLeft, (leftA * topC - topA * leftC) / detTopLeft)
	 
		// Intersection of top and right
		double detTopRight = rightA * topB - rightB * topA
		def ptTopRight = new Point((topB * rightC - rightB * topC) / detTopRight, (rightA * topC - topA * rightC) / detTopRight)
	 
		// Intersection of right and bottom
		double detBottomRight = rightA * bottomB - rightB * bottomA
		def ptBottomRight = new Point((bottomB * rightC - rightB * bottomC) / detBottomRight, (rightA * bottomC - bottomA * rightC) / detBottomRight)
	 
		// Intersection of bottom and left
		double detBottomLeft = leftA * bottomB - leftB * bottomA
		def ptBottomLeft = new Point((bottomB * leftC - leftB * bottomC) / detBottomLeft, (leftA * bottomC - bottomA * leftC) / detBottomLeft)
	
		Core.line(img, ptTopRight, ptTopRight, new Scalar(255), 10)
		Core.line(img, ptTopLeft, ptTopLeft, new Scalar(255), 10)
		Core.line(img, ptBottomRight, ptBottomRight, new Scalar(255), 10)
		Core.line(img, ptBottomLeft, ptBottomLeft, new Scalar(255), 10)
		
		// Correct the perspective transform
		int maxLength = (ptBottomLeft.x - ptBottomRight.x) * (ptBottomLeft.x - ptBottomRight.x) + (ptBottomLeft.y - ptBottomRight.y) * (ptBottomLeft.y - ptBottomRight.y)
		
		int temp = (ptTopRight.x - ptBottomRight.x) * (ptTopRight.x - ptBottomRight.x) + (ptTopRight.y - ptBottomRight.y) * (ptTopRight.y - ptBottomRight.y)
		if(temp > maxLength) 
			maxLength = temp
	
		temp = (ptTopRight.x - ptTopLeft.x) * (ptTopRight.x - ptTopLeft.x) + (ptTopRight.y - ptTopLeft.y) * (ptTopRight.y - ptTopLeft.y)
		if(temp > maxLength) 
			maxLength = temp
	
		temp = (ptBottomLeft.x - ptTopLeft.x) * (ptBottomLeft.x - ptTopLeft.x) + (ptBottomLeft.y - ptTopLeft.y) * (ptBottomLeft.y - ptTopLeft.y)
		if(temp > maxLength)
			maxLength = temp
	
		maxLength = Math.sqrt(maxLength)
	
		def margin = (maxLength / 18.0) / 2.0
		
		ptTopLeft.x -= margin
		ptTopLeft.y -= margin
		
		ptTopRight.x += margin
		ptTopRight.y -= margin
		
		ptBottomLeft.x -= margin
		ptBottomLeft.y += margin
		
		ptBottomRight.x += margin
		ptBottomRight.y += margin
		
		maxLength += (margin * 2)
		
		def src = new MatOfPoint2f(ptTopLeft, ptTopRight, ptBottomRight, ptBottomLeft)
		def dst = new MatOfPoint2f(new Point(0,0), new Point(maxLength-1, 0), new Point(maxLength-1, maxLength-1), new Point(0, maxLength-1))
		
		def result = new Mat()
		Imgproc.warpPerspective(img, result, Imgproc.getPerspectiveTransform(src, dst), new Size(maxLength, maxLength))
		
		return result
	}
}
