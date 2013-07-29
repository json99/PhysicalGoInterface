package nu.nsson.go.gui

import groovy.swing.SwingBuilder
import java.awt.BorderLayout as BL
import java.awt.Dimension
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.image.BufferedImage;

import static javax.swing.JFrame.EXIT_ON_CLOSE
import javax.swing.ImageIcon
import javax.swing.text.MaskFormatter.UpperCaseCharacter;

import nu.nsson.go.engine.GoEngineGuiUpdate;
import nu.nsson.go.util.OpenCVConverters

class GUIMain implements GoEngineGuiUpdate {
	def gameEngine
	
	// GUI elements
	def physicalBoard
	def computerBoard
	def computerMove
	
	// Game elements
	def cameraOn = false
	
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
				panel(constraints:BL.CENTER) {
					flowLayout()
					button(action: action(name: 'Load Image', closure: {
						gameEngine.loadImage()
					}))
					
					button(action: action(name: 'Save Image', closure: {
						gameEngine.saveImage()
					}))
					
					button(action: action(name: 'Toggle Camera', closure: {
						if(cameraOn) {
							cameraOn = false
							gameEngine.stopCamera()
						}
						else {
							cameraOn = true
							gameEngine.startCamera()
						}
					}))
					
					button(action: action(name: 'Convert to Grey', closure: {
						gameEngine.convertToGrey()
					}))
					
					button(action: action(name: 'Add Gaussian Blur', closure: {
						gameEngine.addGaussianBlur()
					}))
					
					button(action: action(name: 'Add Adaptive Treshold Filter', closure: {
						gameEngine.addAdaptiveTresholdFilter()
					}))
					
					button(action: action(name: 'Add Dilate', closure: {
						gameEngine.addDilate()
					}))
					
					button(action: action(name: 'Find Largest Blob', closure: {
						gameEngine.findLargestBlob()
					}))
					
					button(action: action(name: 'Detect Lines', closure: {
						gameEngine.detectLines()
					}))
				}
				panel(constraints:BL.EAST) {
					borderLayout()
					computerBoard = label(constraints:BL.NORTH)
					computerMove = label(constraints:BL.CENTER)
					panel(constraints:BL.SOUTH) {
						borderLayout()
						button(constraints:BL.WEST, action: action(name: 'Generate Black Move', closure: {
							gameEngine.generateBlackMove()
						
						}))
						
						button(constraints:BL.EAST, action: action(name: 'Generate White Move', closure: {
							gameEngine.generateWhiteMove()
						
						}))
					}
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
	
	public void computerMoveUpdate(String move) {
		computerMove.text = move - "=" - " "
	}
}
