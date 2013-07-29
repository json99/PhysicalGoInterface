package nu.nsson.go.camera

import org.opencv.core.Mat

interface WebCamWrapperUpdate {
	void newImage(Mat img)
}
