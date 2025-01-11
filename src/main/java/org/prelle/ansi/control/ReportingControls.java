package org.prelle.ansi.control;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;

import org.prelle.ansi.ANSIOutputStream;
import org.prelle.ansi.C0Code;
import org.prelle.ansi.C1Code;
import org.prelle.ansi.DeviceControlFragment;
import org.prelle.ansi.StringMessageFragment;
import org.prelle.ansi.commands.DeviceAttributes;
import org.prelle.ansi.commands.DeviceAttributes.Variant;
import org.prelle.ansi.commands.QueryRIPScrip.RipState;
import org.prelle.ansi.commands.QueryRIPScrip;
import org.prelle.ansi.commands.iterm.ITermCommandFragment;
import org.prelle.ansi.commands.iterm.SendITermImage;
import org.prelle.ansi.commands.xterm.XTermWindowOperation;
import org.prelle.ansi.commands.xterm.XTermWindowOperation.Operation;
import org.prelle.ansi.commands.xterm.XtermTextParameter;

public class ReportingControls {

	protected final static Logger logger = System.getLogger(ReportingControls.class.getPackageName());

	public static void requestDeviceAttributesPrimary(ANSIOutputStream out) throws IOException {
		out.write(new DeviceAttributes(Variant.Primary));
//		out.write(C0Code.ESC.code());
//		out.write("[0c".getBytes(StandardCharsets.ISO_8859_1));
	}

	public static void requestDeviceAttributesSecondary(ANSIOutputStream out) throws IOException {
		out.write(new DeviceAttributes(Variant.Secondary));
//		out.write(C0Code.ESC.code());
//		out.write("[>0c".getBytes(StandardCharsets.ISO_8859_1));
	}

	public static void requestDeviceAttributesTertiary(ANSIOutputStream out) throws IOException {
		out.write(new DeviceAttributes(Variant.Tertiary));
//		out.write(C0Code.ESC.code());
//		out.write("[=0c".getBytes(StandardCharsets.ISO_8859_1));
	}

	//-------------------------------------------------------------------
	/**
	 * See https://invisible-island.net/xterm/ctlseqs/ctlseqs.html
	 * @param out
	 * @throws IOException
	 */
	public static void requestColorCount(ANSIOutputStream out) throws IOException {
		logger.log(Level.DEBUG, "requestColorCount (xterm XTGETTCAP)");
		// Query "Co" (Color count)
		DeviceControlFragment dcs = new DeviceControlFragment("+", null, "q436f");
		out.write(dcs);

		// Potential answer
		// DCS([1],r436f=323536 )
	}

	//-------------------------------------------------------------------
	/**
	 * See https://invisible-island.net/xterm/ctlseqs/ctlseqs.html
	 * @param out
	 * @throws IOException
	 */
	public static void requestTermInfo(ANSIOutputStream out) throws IOException {
		logger.log(Level.DEBUG, "requestTermInfo (xterm XTGETTCAP)");
		// Query "TN" (terminal name)
		DeviceControlFragment dcs = new DeviceControlFragment("+", null, "q544e");
		out.write(dcs);

		// Potential answer
		// DCS([1],r544e=787465726d2d6b69747479 )
		// ... tn=xterm-kitty
	}

	//-------------------------------------------------------------------
	public static void requestTopBottomMargin(ANSIOutputStream out) throws IOException {
		logger.log(Level.DEBUG, "requestLeftRightMargin (xterm DECRQSS)");
		// Query "TN" (terminal name)
		DeviceControlFragment dcs = new DeviceControlFragment("$", null, "qr");
		out.write(dcs);

		// Potential answer
		// DCS([1],r544e=787465726d2d6b69747479 )
		// ... tn=xterm-kitty
	}

	//-------------------------------------------------------------------
	public static void requestLeftRightMargin(ANSIOutputStream out) throws IOException {
		logger.log(Level.DEBUG, "requestLeftRightMargin (xterm DECRQSS)");
		// Query "TN" (terminal name)
		DeviceControlFragment dcs = new DeviceControlFragment("$", null, "qs");
		out.write(dcs);

		// Potential answer
		// DCS([1],r544e=787465726d2d6b69747479 )
		// ... tn=xterm-kitty
	}

	//-------------------------------------------------------------------
	/**
	 * See https://sw.kovidgoyal.net/kitty/graphics-protocol/
	 * @param out
	 * @throws IOException
	 */
	public static void requestKittyInlineGraphicsSupport(ANSIOutputStream out) throws IOException {
		logger.log(Level.DEBUG, "requestKittyInlineGraphicsSupport ");
		StringMessageFragment apc = new StringMessageFragment(C1Code.APC, "Gi=31,s=1,v=1,a=q,t=d,f=24;AAAA");
		out.write(apc);

//		out.write(C0Code.ESC.code());
//		out.write("_Gi=31,s=1,v=1,a=q,t=d,f=24;AAAA".getBytes(StandardCharsets.ISO_8859_1));
//		out.write(C0Code.ESC.code());
//		out.write((int)'\\');
	}

	//-------------------------------------------------------------------
	/**
	 * See https://iterm2.com/documentation-images.html
	 * @param out
	 * @throws IOException
	 */
	public static void requestITermImages(ANSIOutputStream out) throws IOException {
		logger.log(Level.DEBUG, "requestITermImages ");
		SendITermImage iip = new SendITermImage();
		iip.setInline(true);
		iip.setImgData(new byte[4] );
		out.write(iip);
	}

	//-------------------------------------------------------------------
	/**
	 * See https://iterm2.com/3.0/documentation-escape-codes.html
	 * @param out
	 * @throws IOException
	 */
	public static void requestITermColors(ANSIOutputStream out) throws IOException {
		logger.log(Level.DEBUG, "requestITermColors ");
		ITermCommandFragment reqBGCol = new ITermCommandFragment("4;-2;?");
		out.write(reqBGCol);
	}

	//-------------------------------------------------------------------
	/**
	 * See https://iterm2.com/3.0/documentation-escape-codes.html
	 * @param out
	 * @throws IOException
	 */
	public static void requestITermCellSize(ANSIOutputStream out) throws IOException {
		logger.log(Level.DEBUG, "requestITermColors ");
		ITermCommandFragment msg = new ITermCommandFragment("1337;ReportCellSize");
		out.write(msg);
	}

	//-------------------------------------------------------------------
	public static void requestXTermWindowSize(ANSIOutputStream out) throws IOException {
		logger.log(Level.DEBUG, "requestXTermWindowSize (Xterm)");
		out.write(new XTermWindowOperation(Operation.REPORT_SIZE_CHAR));
	}

	//-------------------------------------------------------------------
	/**
	 * Report size of the screen in pixels.
          Result is CSI  5 ;  height ;  width t
	 * @param out
	 * @throws IOException
	 */
	public static void requestXTermCellSize(ANSIOutputStream out) throws IOException {
		logger.log(Level.DEBUG, "requestXTermCellSize (Xterm)");
		out.write(new XTermWindowOperation(Operation.REPORT_CHARSIZE_PIXEL));
	}

	//-------------------------------------------------------------------
	public static void requestXTermFontSize(ANSIOutputStream out) throws IOException {
		logger.log(Level.DEBUG, "requestXTermFontSize (Xterm)");
		out.write(new XtermTextParameter(XtermTextParameter.FONT,"?"));
	}
    //-------------------------------------------------------------------
    /**
     * @param out
     * @throws IOException
     */
    public static void requestRIPScrip(ANSIOutputStream out) throws IOException {
        logger.log(Level.INFO, "requestRIPScrip ");
        out.write(new QueryRIPScrip(RipState.QUERY));

//      out.write(C0Code.ESC.code());
//      out.write("_Gi=31,s=1,v=1,a=q,t=d,f=24;AAAA".getBytes(StandardCharsets.ISO_8859_1));
//      out.write(C0Code.ESC.code());
//      out.write((int)'\\');
    }

}
