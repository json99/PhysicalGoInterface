package nu.nsson.go.engine

import java.awt.image.BufferedImage;

interface GoEngineGuiUpdate {
	public void webCamBoardUpdate(BufferedImage board)
	public void computerBoardUpdate(String board)
	public void computerMoveUpdate(String move)
}
