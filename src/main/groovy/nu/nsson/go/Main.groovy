package nu.nsson.go

import nu.nsson.go.helper.LibLoader
import nu.nsson.go.gui.GUIMain
import nu.nsson.go.engine.PhysicalGoInterfaceEngine

// Initialize OpenCV, needed to be in a real java class
LibLoader.load()

// Initialize game engine
def gameEngine = new PhysicalGoInterfaceEngine()

// Initialize GUI
def guiMain = new GUIMain(gameEngine)
guiMain.initialize()

gameEngine.start()