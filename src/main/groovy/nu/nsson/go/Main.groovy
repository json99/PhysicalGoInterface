package nu.nsson.go

import nu.nsson.go.helper.LibLoader
import nu.nsson.go.gui.GUIMain
import nu.nsson.go.camera.WebCamWrapper

// Initialize OpenCV, needed to be in a real java class
LibLoader.load()

// Initialize WebCam
def cam = WebCamWrapper.getInstance()

// Initialize GUI
def guiMain = new GUIMain(cam)
guiMain.initialize()