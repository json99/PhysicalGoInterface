package nu.nsson.go

import java.awt.image.BufferedImage

interface WebCamWrapperUpdate {
	void newImage(BufferedImage img)
}
