package nu.nsson.go.gui

import groovy.swing.SwingBuilder
import java.awt.BorderLayout as BL

class GUIMain {
	def count = 0
	
	public GUIMain() {
		new SwingBuilder().build {
		  frame(title:'PhysicalGoInterface', size:[1024,768], show: true) {
			borderLayout()
			textlabel = label(text:"Click the button!", constraints: BL.NORTH)
			button(text:'Click Me',
				 actionPerformed: {count++; textlabel.text = "Clicked ${count} time(s)."; println "clicked"},
				 constraints:BL.SOUTH)
		  }
		}
	}
}
