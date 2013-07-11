package nu.nsson.go.gui

import groovy.swing.SwingBuilder
import java.awt.BorderLayout as BL
import java.awt.image.BufferedImage;

import static javax.swing.JFrame.EXIT_ON_CLOSE
import javax.swing.ImageIcon

import nu.nsson.go.engine.GoEngineGuiUpdate;
import nu.nsson.go.util.OpenCVConverters

class GUIMain implements GoEngineGuiUpdate {
	def gameEngine
	
	// GUI elements
	def physicalBoard
	def computerBoard
	
	public GUIMain(engine) {
		gameEngine = engine
		gameEngine.delegate = this
	}
	
	def initialize() {
		new SwingBuilder().build {
			frame(title:'PhysicalGoInterface', size:[2300,1280], show: true, defaultCloseOperation: EXIT_ON_CLOSE) {
				borderLayout()
				
				
				panel(constraints:BL.WEST) {
					borderLayout()
					physicalBoard = label(constraints:BL.CENTER)
				}
				panel(constraints:BL.EAST) {
					borderLayout()
					computerBoard = label(constraints:BL.CENTER)
				}
			}
		}
	}

	public void webCamBoardUpdate(BufferedImage board) {
		def icon = new ImageIcon(board)
		physicalBoard.icon = icon
	}

	public void computerBoardUpdate(String board) {
		computerBoard.text = board
	}
}
