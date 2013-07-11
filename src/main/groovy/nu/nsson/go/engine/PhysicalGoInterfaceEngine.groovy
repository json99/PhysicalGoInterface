package nu.nsson.go.engine

import java.awt.image.BufferedImage;

import net.sf.gogui.gtp.GtpClient.IOCallback;
import nu.nsson.go.WebCamWrapperUpdate
import nu.nsson.go.camera.WebCamWrapper
import nu.nsson.go.gnugo.GnuGoWrapper

class PhysicalGoInterfaceEngine implements WebCamWrapperUpdate, IOCallback {
	
	private GoEngineGuiUpdate delegate
	
	def webCam
	def gnuGo
	
	public PhysicalGoInterfaceEngine() {
		// Initialize WebCam
		webCam = WebCamWrapper.getInstance()
		webCam.delegate = this
		
		// Initialize Gnu Go
		gnuGo = new GnuGoWrapper(this)
	}
	
	def start() {
		webCam.start()
		
		// Show initial board
		gnuGo.showBoard()
	}
	
	def stop() {
		webCam.stop()
	}
	
	public void setDelegate(GoEngineGuiUpdate d) {
		delegate = d
	}
	
	public GoEngineGuiUpdate getDelegate() {
		return delegate
	}

	public void newImage(BufferedImage img) {
		if(delegate) {
			delegate.webCamBoardUpdate(img)
		}
	}

	public void receivedInvalidResponse(String msg) {
		println "Invalid: ${msg}"
	}
	public void receivedResponse(boolean arg0, String msg) {
		println "Response: ${msg}"
		
		if(delegate) {
			delegate.computerBoardUpdate("<html><pre>${msg}</pre></html>")
		}
	}
	public void receivedStdErr(String msg) {
		println "StdErr: ${msg}"
	}
	public void sentCommand(String cmd) {
		println "Cmd: ${cmd}"
	}
	
}
