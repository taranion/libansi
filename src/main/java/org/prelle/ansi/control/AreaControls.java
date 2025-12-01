package org.prelle.ansi.control;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.prelle.ansi.ANSIOutputStream;
import org.prelle.ansi.commands.EraseInDisplay;
import org.prelle.ansi.commands.EraseInLine;
import org.prelle.ansi.commands.EraseRectangularArea;
import org.prelle.ansi.commands.FillRectangularArea;
import org.prelle.ansi.commands.LinefeedNewlineMode;
import org.prelle.ansi.commands.SetLeftAndRightMargin;
import org.prelle.ansi.commands.SetTopAndBottomMargin;

/**
 *
 */
public class AreaControls {

	protected final static Logger logger = System.getLogger(DisplayControl.class.getPackageName());

	//-------------------------------------------------------------------
	public static void setLeftAndRightMargins(ANSIOutputStream out, int pl, int pr) throws IOException {
		logger.log(Level.DEBUG, "setLeftAndRightMargins");
		if (pr>0 && pr<=pl) throw new IllegalArgumentException("Right border must be greater than left border");
		// CSI Pl ; Pr s
		//out.writeCSI(ControlFunction.DECSLRM, pl, pr);
//		out.writeCSI(new SetLeftAndRightMargin(pl,pr));
		out.write(new SetLeftAndRightMargin(pl,pr));
//		byte[] data = SetLeftAndRightMargin.encode(null,null, SetL);
//		out.write(data, "DECSLRM");
	}

	//-------------------------------------------------------------------
	public static void setTopAndBottomMargins(ANSIOutputStream out, int pt, int pb) throws IOException {
		if (pb>0 && pb<=pt) throw new IllegalArgumentException("Bottom border must be greater than top border");
		// CSI Pt ; Pb r
		//out.writeCSI(ControlFunction.DECSTBM, pt, pb);
		out.write(new SetTopAndBottomMargin(pt,pb));
	}

	//-------------------------------------------------------------------
	public static void clearScreen(ANSIOutputStream out) throws IOException {
		// CSI 2 J
		//out.writeCSI(ControlFunction.ED, 2);
		out.write(new EraseInDisplay(EraseInDisplay.Mode.SCREEN));
	}

	//-------------------------------------------------------------------
	public static void clearFromHere(ANSIOutputStream out) throws IOException {
		// CSI 0 J
		//out.writeCSI(ControlFunction.ED, 0);
		out.write(new EraseInLine(EraseInLine.Mode.LINE_FROM_CURSOR));
	}

	//-------------------------------------------------------------------
	public static void clearToHere(ANSIOutputStream out) throws IOException {
		// CSI 1 J
		//out.writeCSI(ControlFunction.ED, 1);
		out.write(new EraseInLine(EraseInLine.Mode.LINE_TO_CURSOR));
	}

	//-------------------------------------------------------------------
	public static void fillArea(ANSIOutputStream out, char c, int x, int y, int w, int h) throws IOException {
		out.write(new FillRectangularArea(c, y, x, y+h, x+w));
	}

	//-------------------------------------------------------------------
	public static void clearArea(ANSIOutputStream out, int x, int y, int w, int h) throws IOException {
		out.write(new EraseRectangularArea(y, x, y+h, x+w));
	}
}
