package nu.nsson.go.gnugo

import groovy.transform.Synchronized

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Scanner

import net.sf.gogui.gtp.GtpClient
import net.sf.gogui.gtp.GtpClient.IOCallback;

import nu.nsson.go.util.PhysicalGoInterfaceProperties

class GnuGoWrapper {
	def gnuGoPath
	def gnuGoClient
	
	def toGnuGoStream
	def fromGnuGoStream
	
	public GnuGoWrapper(IOCallback callback) {
		def config = PhysicalGoInterfaceProperties.getInstance()
		
		gnuGoPath = config.getGnuGoPath("./gnugo/gnugo-3.8 --mode gtp")
		
		gnuGoClient = new GtpClient(gnuGoPath, null, false, callback)
	}
	
	def showBoard() {
		gnuGoClient.send("showboard")
	}
	
	def generateBlackMove() {
		gnuGoClient.send("genmove_black")
	}
	
	def generateWhiteMove() {
		gnuGoClient.send("genmove_white")
	}
}
