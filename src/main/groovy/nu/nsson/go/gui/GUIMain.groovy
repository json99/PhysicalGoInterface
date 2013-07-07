package nu.nsson.go.gui

import groovy.swing.SwingBuilder
import java.awt.BorderLayout as BL
import static javax.swing.JFrame.EXIT_ON_CLOSE
import javax.swing.ImageIcon

import nu.nsson.go.util.OpenCVConverters

class GUIMain {
	def cam
	
	public GUIMain(c) {
		cam = c
	}
	
	def initialize() {
		new SwingBuilder().build {
			frame(title:'PhysicalGoInterface', size:[1024,768], show: true, defaultCloseOperation: EXIT_ON_CLOSE) {
				borderLayout()
				
				
				panel(constraints:BL.WEST) {
					borderLayout()
					board = label(text:'Testing', constraints:BL.CENTER)
				}
				panel(constraints:BL.EAST) {
					
				}
				panel(constraints:BL.SOUTH) {
					gridLayout(columns:7, rows:1) 
					button(text:'Update',
						actionPerformed: {
							def img = cam.captureImage()
							def buffImg = OpenCVConverters.matToBufferedImage(img)
							def icon = new ImageIcon(buffImg)
							
							board.icon = icon
						})
				}
			}
		}
	}
}
