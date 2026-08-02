package org.prelle.ansi;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.prelle.ansi.DeviceAttributes.OperatingLevel;
import org.prelle.ansi.DeviceAttributes.VT220Parameter;
import org.prelle.ansi.commands.CursorPosition;
import org.prelle.ansi.commands.CursorPositionReport;
import org.prelle.ansi.commands.DeviceAttributes;
import org.prelle.ansi.commands.DeviceAttributes.Variant;
import org.prelle.ansi.commands.DeviceStatusReport;
import org.prelle.ansi.commands.ModeState;
import org.prelle.ansi.commands.ResetMode;
import org.prelle.ansi.commands.SetMode;
import org.prelle.ansi.commands.SetMode.ANSIMode;
import org.prelle.ansi.commands.kitty.KittyScaledFontFragment;
import org.prelle.ansi.commands.xterm.XTermWindowOperation;
import org.prelle.ansi.control.AreaControls;
import org.prelle.ansi.control.CursorControls;
import org.prelle.ansi.control.DisplayControl;
import org.prelle.ansi.control.ReportingControls;

/**
 *
 */
public class CapabilityDetector2 {
	
	private static enum State {
		IDLE,
		STARTED,
		FINISHED,
	}

	private static enum Step {
		TERMINFO,
		TERMNAME,
		XTERM_WINDOWSIZE,
		XTERM_CHARSIZE,
		XTERM_FONTSIZE,
		ITERM_IMAGES,
		KITTY,
        RIP_SCRIP,
		TOP_BOTTOM_MARGIN,
		LEFT_RIGHT_MARGIN,
		DEVICE_ATTRIBUTES,  // 10
		DEVICE_ATTRIBUTES2,
		CURSOR_POSITIONING,
		KITTY_TEXT_SCALING, // 13
	}

	private final static Logger logger = System.getLogger(CapabilityDetector2.class.getPackageName());

	private ANSIOutputStream out;
	private ANSIInputStream in;
	private Consumer<TerminalCapabilities> listener;

	private State state;
	private TerminalCapabilities capabilities;
	private static List<Integer> stepsTaken = new ArrayList<>();
	private static int expectedCPRs;

	private StringBuffer printable = new StringBuffer();
	private boolean continueReading = false;
	private List<AParsedElement> processLater = new ArrayList<>();

	//-------------------------------------------------------------------
	public CapabilityDetector2(ANSIInputStream in, ANSIOutputStream out, Consumer<TerminalCapabilities> listener) {
		this.in = in;
		this.out= out;
		this.listener = listener;
		this.state = State.IDLE;
		capabilities = new TerminalCapabilities();
		//out.setLoggingListener( (k,v) -> System.err.println("-->"+k+" : "+v));
	}

	//-----------------------------------------------------------------
	private void startReadFromSocketThread() {
		Runnable run = () -> {
			continueReading = true;
			while (continueReading) {
				try {
					AParsedElement data = in.readFragment();
					if (data!=null) {
						processLater.addAll( process(data) );
					}
				} catch (SocketTimeoutException timeout) {
					// Ignore, this is expected
				} catch (IOException e) {
					logger.log(Level.ERROR, "Error reading from socket", e);
					continueReading = false;
				}
			}
			logger.log(Level.ERROR, "Stopping read from socket thread");
		};
		Thread.startVirtualThread(run);
	}
	
	//-------------------------------------------------------------------
	public void start(int timeoutMs) {
		logger.log(Level.WARNING, "ENTER: start({0})", timeoutMs);
		try {
			state = State.STARTED;
			startReadFromSocketThread();
			performCheck(timeoutMs, 80, 24);
			state = State.FINISHED;
			listener.accept(capabilities);
			logger.log(Level.WARNING, "Stop detecting terminal capabilities");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			logger.log(Level.WARNING, "LEAVE: start({0})", timeoutMs);
		}
	}

	//-------------------------------------------------------------------
	public TerminalCapabilities performCheck(int timeoutMS, int width, int height) throws IOException {
		logger.log(Level.INFO, "ENTER: detecting terminal capabilities");
		try {
		    printable.delete(0, printable.length());
//			out.write("If your clients terminal does not fully support VT100/ANSI, you may see weird output below.\r\n");
//			out.write("You can safely ignore that.\r\n");
//			out.write(new SelectGraphicRendition(List.of(Meaning.INVISIBLE_ON)));
//			out.write(new SelectGraphicRendition(List.of(Meaning.FAINT_ON)));
			setLocalEcho(false);
			out.writeCSI('m', 30);

			waitFor(Step.TERMINFO);
			ReportingControls.requestTermInfo(out);
			ReportingControls.requestColorCount(out);
			out.write("\n");
			out.flush();
            Thread.sleep(50);
			waitFor(Step.XTERM_CHARSIZE);
			testCellSize();
			out.flush();
			waitFor(Step.XTERM_WINDOWSIZE);
			testWindowSize();
			out.flush();
			waitFor(Step.XTERM_FONTSIZE);
			ReportingControls.requestXTermFontSize(out);
			out.write("\n");
			out.flush();
            Thread.sleep(50);
//			waitFor(Step.ITERM_IMAGES);
//			ReportingControls.requestITermCellSize(out);
//			out.write("\n");
//			out.flush();
//            Thread.sleep(50);
			waitFor(Step.KITTY);
			ReportingControls.requestKittyInlineGraphicsSupport(out);
			out.write("\n");
			out.flush();
            Thread.sleep(50);
//			System.exit(1);
            waitFor(Step.RIP_SCRIP);
            ReportingControls.requestRIPScrip(out);
            out.flush();
            Thread.sleep(100);
			waitFor(Step.TOP_BOTTOM_MARGIN);
            logger.log(Level.DEBUG, "testTopBottomMargins");
			testTopBottomMargins(height);
			out.flush();
			waitFor(Step.LEFT_RIGHT_MARGIN);
			testLeftRightMargins(width);
			out.flush();
			waitFor(Step.DEVICE_ATTRIBUTES);
			logger.log(Level.DEBUG, "testDeviceAttributes");
			ReportingControls.requestDeviceAttributesPrimary(out);
			out.flush();
			waitFor(Step.DEVICE_ATTRIBUTES2);
			ReportingControls.requestDeviceAttributesSecondary(out);
			out.flush();
			waitFor(Step.CURSOR_POSITIONING);
			testCursorPositioning();
			capabilities.inlineImageSixel = capabilities.features.contains(VT220Parameter.SIXEL);
			waitFor(Step.KITTY_TEXT_SCALING);
			testKittyFontScaling();
			out.flush();

//			out.write(new DECSelectStatusDisplayType(DECSelectStatusDisplayType.Mode.HOST_WRITABLE_STATUS_LINE));
//			out.write(new DECSelectActiveStatusDisplay(DECSelectActiveStatusDisplay.Mode.STATUS_LINE));
//			out.write("Hallo");
//			out.write(new DECSelectActiveStatusDisplay(DECSelectActiveStatusDisplay.Mode.MAIN_DISPLAY));
			out.flush();

			synchronized (stepsTaken) {
				// Wait 1000 milliseconds on a non-blocking socket
				try {
					logger.log(Level.WARNING, "Wait {0}ms for all responses", timeoutMS);
					Instant start = Instant.now();
					stepsTaken.wait(timeoutMS);
					logger.log(Level.WARNING, "Done waiting ... {0}ms", Instant.now().toEpochMilli()-start.toEpochMilli());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					continueReading = false;
				}
			}
			AreaControls.clearScreen(out);
			out.writeCSI('m', 0);

//			logger.log(Level.INFO, "Reporting");
//			capabilities.report(out);
			capabilities.report(new ANSIOutputStream(System.out));
		} catch (SocketException e) {
			logger.log(Level.ERROR, "Failed in detection: "+e.getMessage(),e);
		} catch (Exception e) {
			logger.log(Level.ERROR, "Failed in detection",e);
		} finally {
			logger.log(Level.INFO, "LEAVE: detecting terminal capabilities");
		}
		return capabilities;
	}

	//-------------------------------------------------------------------
	private void waitFor(Step step) {
		stepsTaken.add(step.ordinal());
		logger.log(Level.DEBUG, "Wait for {0}/{1} = {2}", step.name(), step.ordinal(), stepsTaken);
	}

	//-------------------------------------------------------------------
	private void acknowledgeStep(Step step) {
		synchronized (stepsTaken) {
//			logger.log(Level.WARNING, "Remove {0}/{1} from {2}", step.name(), step.ordinal(), stepsTaken);
			(new ArrayList<Integer>(stepsTaken)).stream().filter(s -> s<=step.ordinal()).forEach(elem -> stepsTaken.remove( (Integer)elem));
//			logger.log(Level.WARNING, "Missing answers to "+stepsTaken);
			if (stepsTaken.isEmpty()) {
				stepsTaken.notifyAll();
			}
		}
	}

	//-------------------------------------------------------------------
	private void setLocalEcho(boolean showEcho) throws IOException {
		if (showEcho) {
			logger.log(Level.DEBUG, "Clear SendReceiveMode");
			out.write(new ResetMode(ANSIMode.SRM_SEND_RECEIVE_MODE));
		} else {
			logger.log(Level.DEBUG, "Set SendReceiveMode");
			out.write(new SetMode(ANSIMode.SRM_SEND_RECEIVE_MODE));
		}
	}

	//-------------------------------------------------------------------
	private List<AParsedElement> process(AParsedElement frag) {
		logger.log(Level.WARNING, "Process {0} in {1}",frag, stepsTaken);
		if (state==State.FINISHED) return List.of(frag);
		
 		// Check if there is a printable result to process
 		if (!printable.isEmpty()) {
 		    String toProcess = printable.toString();
            logger.log(Level.DEBUG, "Process {0}",toProcess);
            if (toProcess.startsWith("RIPSCRIP")) {
                acknowledgeStep(Step.RIP_SCRIP);
                capabilities.ripScrip=true;
                String versionS = toProcess.substring(8,12);
                capabilities.ripScripVersion = Integer.parseInt(versionS);
                logger.log(Level.INFO, "Found a RIPScrip terminal for version {0}.{1}", capabilities.ripScripVersion/100, capabilities.ripScripVersion%100);
            }

            printable.delete(0, printable.length());
 		}

		logger.log(Level.INFO, "Received {0} in {1}",frag, stepsTaken);
		switch (frag) {
		case DeviceAttributes da -> {
			if (da.getVariant()==Variant.Primary_Response) {
				logger.log(Level.DEBUG, "Featurecodes: {0}", da.getArguments());
				capabilities.operatingLevel = OperatingLevel.valueOf(da.getArguments().get(0));
				logger.log(Level.INFO, "Operating level: {0}", capabilities.operatingLevel);
				for (int i=1; i<da.getArguments().size(); i++) {
					try {
						VT220Parameter p = VT220Parameter.valueOf(da.getArguments().get(i));
						if (p==null) {
							logger.log(Level.ERROR, "Unknown VT220 parameter {0}", da.getArguments().get(i));
							logger.log(Level.ERROR, "Primary Device Attribute Featurecodes: {0}", da.getArguments());
						} else if (!capabilities.features.contains(p))
							capabilities.features.add( p );
					} catch (Exception e) {
						logger.log(Level.WARNING, "Unknown VT220 parameter {0}", da.getArguments().get(i));
						logger.log(Level.DEBUG, "Primary Device Attribute Featurecodes: {0}", da.getArguments());
					}
				}
				if (capabilities.features.contains(VT220Parameter.SIXEL))
					capabilities.inlineImageSixel=true;
				logger.log(Level.INFO, "Features       : {0}", capabilities.features);
				acknowledgeStep(Step.DEVICE_ATTRIBUTES);
			} else if (da.getVariant()==Variant.Secondary) {
				capabilities.generalCompatibility = org.prelle.ansi.DeviceAttributes.TerminalType.valueOf(da.getArguments().get(0));
				logger.log(Level.INFO, "General compatibility: {0}", capabilities.generalCompatibility);
				acknowledgeStep(Step.DEVICE_ATTRIBUTES2);
			}
		}
		case CursorPositionReport cpr -> {
			logger.log(Level.DEBUG, "Line/X="+cpr.getColumn()+"  y="+cpr.getLine()+"   expected = "+expectedCPRs);
			if (expectedCPRs>0)
				expectedCPRs--;
			if (stepsTaken.contains(Step.TOP_BOTTOM_MARGIN.ordinal())) {
				capabilities.marginTopBottom = cpr.getLine()==15;
				capabilities.cursorPositioning=true;
				logger.log(Level.INFO, "Top Bottom Margin = "+capabilities.marginTopBottom);
				acknowledgeStep(Step.TOP_BOTTOM_MARGIN);
			} else if (stepsTaken.contains(Step.LEFT_RIGHT_MARGIN.ordinal())) {
				capabilities.marginLeftRight = cpr.getColumn()==5;
				capabilities.cursorPositioning=true;
				logger.log(Level.INFO, "Left Right Margin = "+capabilities.marginLeftRight);
				acknowledgeStep(Step.LEFT_RIGHT_MARGIN);
			} else if (stepsTaken.contains(Step.CURSOR_POSITIONING.ordinal())) {
				if (capabilities.cursorPositioning==false) {
					capabilities.cursorPositioning = cpr.getColumn()==50;
					acknowledgeStep(Step.CURSOR_POSITIONING);
				}
				logger.log(Level.INFO, "Cursor positioning = "+capabilities.cursorPositioning);
				if (expectedCPRs==2) {
					acknowledgeStep(Step.CURSOR_POSITIONING);
				}
			} else if (stepsTaken.contains(Step.KITTY_TEXT_SCALING.ordinal())) {
				if (capabilities.scaledTextKitty==false)
					capabilities.scaledTextKitty = cpr.getColumn()>=30;
				logger.log(Level.INFO, "Text scaling = "+capabilities.scaledTextKitty);
				if (expectedCPRs==0) {
					acknowledgeStep(Step.KITTY_TEXT_SCALING);
				}
			} else {
				logger.log(Level.INFO, "Not knowing");
				expectedCPRs--;
			}
		}
		case DeviceControlFragment dcs -> processDeviceControl(dcs);
		case StringMessageFragment dcs when dcs.getCode()==C1Code.APC -> processApplicationCommand(dcs);
		case StringMessageFragment dcs when dcs.getCode()==C1Code.PM  -> processPrivacyMessage(dcs);
		case StringMessageFragment dcs when dcs.getCode()==C1Code.OSC -> processOperatingSystemCommand(dcs);
		case XTermWindowOperation winOP -> processWinOP(winOP);
//		case ControlSequenceFragment seq -> processControlSequence( seq );
		default -> {
			logger.log(Level.WARNING, "Not handling {0} / {1}", frag, frag.getClass());
			return List.of(frag);
		}
		}
		return List.of();
	}
	//-------------------------------------------------------------------
	private void testCursorPositioning() throws IOException {
		logger.log(Level.DEBUG, "testCursorPositioning");
		CursorControls.savePositionDEC(out);
//		out.write("\033[6n");
		expectedCPRs=2;
		out.write(new DeviceStatusReport(DeviceStatusReport.Type.CURSOR_POS));
//		CursorPositionReport rep1 = (CursorPositionReport) in.readFragment();
//
		out.write(new CursorPosition(50,13));
		out.write(new DeviceStatusReport(DeviceStatusReport.Type.CURSOR_POS));
//		CursorPositionReport rep2 = (CursorPositionReport) in.readFragment();
//		cursorPositioning = rep2.getLine()==13;
//		logger.log(Level.INFO, "Supports cursor positioning: {0}", cursorPositioning);
//		System.exit(1);
		CursorControls.restorePositionDEC(out);
	}

	//-------------------------------------------------------------------
	private void testKittyFontScaling() throws IOException {
		logger.log(Level.DEBUG, "testKittyFontScaling");
		CursorControls.savePositionDEC(out);
		out.write("\r\n");
		expectedCPRs+=2;
		out.write(new DeviceStatusReport(DeviceStatusReport.Type.CURSOR_POS));
//		CursorPositionReport rep1 = (CursorPositionReport) in.readFragment();
//
		KittyScaledFontFragment scaled = new KittyScaledFontFragment(3, "Scaled Font");
		out.write(scaled);

		out.write(new DeviceStatusReport(DeviceStatusReport.Type.CURSOR_POS));
//		CursorPositionReport rep2 = (CursorPositionReport) in.readFragment();
//		cursorPositioning = rep2.getLine()==13;
//		logger.log(Level.INFO, "Supports cursor positioning: {0}", cursorPositioning);
//		System.exit(1);
		CursorControls.restorePositionDEC(out);
	}

	//-------------------------------------------------------------------
	private void testTopBottomMargins(int height) throws IOException {
		logger.log(Level.DEBUG, "ENTER: testTopBottomMargins");
//		out.write(new DeviceStatusReport(DeviceStatusReport.Type.CURSOR_POS));
		CursorControls.savePositionDEC(out);
//		out.write("\033[6n");
		AreaControls.setTopAndBottomMargins(out, 10, 15);
		out.write(new CursorPosition(1,11));
		for (int i=0; i<10; i++) out.write("\r\n");
		expectedCPRs+=1;
		out.write(new DeviceStatusReport(DeviceStatusReport.Type.CURSOR_POS)); // Should be 15 (not 21)
		out.write("\n");
		out.flush();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AreaControls.setTopAndBottomMargins(out, 1, height);
		CursorControls.restorePositionDEC(out);
		logger.log(Level.DEBUG, "LEAVE: testTopBottomMargins");
	}

	//-------------------------------------------------------------------
	private void testCellSize() throws IOException {
		logger.log(Level.ERROR, "ENTER: testCellSize");
		ReportingControls.requestXTermCellSize(out);
		out.flush();
		logger.log(Level.ERROR, "LEAVE: testCellSize");
	}

	//-------------------------------------------------------------------
	private void testWindowSize() throws IOException {
		logger.log(Level.DEBUG, "ENTER: testWindowSize");
		ReportingControls.requestXTermWindowSize(out);
		out.flush();
		logger.log(Level.DEBUG, "LEAVE: testWindowSize");
	}

	//-------------------------------------------------------------------
	private void testLeftRightMargins(int width) throws IOException {
		logger.log(Level.DEBUG, "ENTER: testLeftRightMargins");
//		out.write(new DeviceStatusReport(DeviceStatusReport.Type.CURSOR_POS));
		CursorControls.savePositionDEC(out);
//		out.write("\033[6n");
		DisplayControl.setLeftRightMarginMode(out, ModeState.SET);
		AreaControls.setLeftAndRightMargins(out, 5, 65);
		out.write(new CursorPosition(5,11));
		for (int i=0; i<10; i++) out.write("\r\n");
		expectedCPRs+=1;
		out.write(new DeviceStatusReport(DeviceStatusReport.Type.CURSOR_POS));
		out.flush();
		try {
			Thread.sleep(60);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AreaControls.setLeftAndRightMargins(out, 1, width);
		DisplayControl.setLeftRightMarginMode(out, ModeState.RESET);
		CursorControls.restorePositionDEC(out);
		logger.log(Level.DEBUG, "LEAVE: testLeftRightMargins");
	}

//	//-------------------------------------------------------------------
//	private void testXterm() throws IOException {
//		XTermControl.reportAll(out);
//	}

	//-------------------------------------------------------------------
	private void testITerm() throws IOException {
//		XTermControl.reportAll(out);
		ReportingControls.requestTermInfo(out);
	}

	//-------------------------------------------------------------------
	private void processDeviceControl(DeviceControlFragment dcs) {

		if ("+".equals(dcs.getIntermediate())) {
			// Xterm DCS
			String data = dcs.getText(StandardCharsets.US_ASCII);
			if (data.startsWith("r")) {
				// Answer to XTGETTCAP
				acknowledgeStep(Step.TERMINFO);
				String[] kv = data.substring(1).split("=");
				StringBuffer key = new StringBuffer();
				StringBuffer value = new StringBuffer();
				for (int i=0; i<kv[0].length(); i+=2) {
					String hex = kv[0].substring(i, i+2);
					key.append( (char) (Integer.parseInt(hex, 16)));
				}
				for (int i=0; i<kv[1].length(); i+=2) {
					String hex = kv[1].substring(i, i+2);
					value.append( (char) (Integer.parseInt(hex, 16)));
				}
				logger.log(Level.INFO, key+" = "+value);
				if ("TN".equals(key.toString())) {
					capabilities.terminalName=value.toString();
					logger.log(Level.INFO, "Terminal name = "+value.toString());
					if (List.of("WezTerm","lociterm").contains(value.toString())) {
						capabilities.inlineImageITerm=true;
						logger.log(Level.WARNING, "Hardcoded iTerm Image protocol support for this terminal: "+value);
					}
				} else if ("Co".equals(key.toString())) {
					int count =0 ;
					if ("16".equals(value.toString())) {
						count=16;
					} else if ("256".equals(value.toString())) {
						count=256;
					} else {
						try {
							count = Integer.parseInt(kv[1], 16);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					logger.log(Level.INFO, "Colors = "+count);
					if (count>=16)
						capabilities.color16=true;
					if (count>=256)
						capabilities.color256=true;
					if (count>=0x1000000)
						capabilities.color16m=true;
				} else {
					logger.log(Level.WARNING, "TODO: XTGETTCAP response {0}={1}", key, value);
				}
				return;
			}
			logger.log(Level.INFO, "data="+data);
		}

		logger.log(Level.WARNING, "TODO: Received DCS ", dcs.getText(StandardCharsets.US_ASCII));

	}

	//-------------------------------------------------------------------
	private void processApplicationCommand(StringMessageFragment apc) {
		logger.log(Level.DEBUG, "Received APC ", apc.toString());

		if (apc.getData().startsWith("G")) {
			// May be Kitty graphics protocol
			if (apc.getData().endsWith(";OK")) {
				logger.log(Level.INFO, "Kitty graphics confirmed");
				capabilities.inlineImageKitty = true;
				acknowledgeStep(Step.KITTY);
			}
		}
	}

	//-------------------------------------------------------------------
	private void processPrivacyMessage(StringMessageFragment apc) {
		logger.log(Level.WARNING, "TODO: Received PM ", apc.toString());
	}

	//-------------------------------------------------------------------
	private void processOperatingSystemCommand(StringMessageFragment osc) {
		if (osc.getData().startsWith("50;")) {
			logger.log(Level.INFO, "Received font report: "+osc.getData().substring(3));
			acknowledgeStep(Step.XTERM_FONTSIZE);
			return;
		}
		if (osc.getData().startsWith("1337;")) {
			logger.log(Level.INFO, "Received  iTerm response: "+osc.getData().substring(3));
			acknowledgeStep(Step.ITERM_IMAGES);
			return;
		}
		logger.log(Level.WARNING, "TODO: Received OSC ", osc.toString());
	}

	//-------------------------------------------------------------------
	private void processWinOP(XTermWindowOperation winOP) {
		int type = winOP.getArguments().get(0);
		switch (type) {
		case 6:
			// Character sell size in pixels
			int height = winOP.getArguments().get(1);
			int width = winOP.getArguments().get(2);
			capabilities.cellHeight = height;
			capabilities.cellWidth  = width;
			logger.log(Level.INFO, "A character cell is {0}x{1} pixels ", width, height);
			acknowledgeStep(Step.XTERM_CHARSIZE);
			break;
		case 8:
			// Window size in characters
			height = winOP.getArguments().get(1);
			width = winOP.getArguments().get(2);
			capabilities.screenWidth = width;
			capabilities.screenHeight = height;
			logger.log(Level.INFO, "The screen has {0}x{1} characters", width, height);
			acknowledgeStep(Step.XTERM_WINDOWSIZE);
			break;
		default:
			logger.log(Level.WARNING, "TODO: Received WinOP "+winOP);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @return the capabilities
	 */
	public TerminalCapabilities getCapabilities() {
		return capabilities;
	}

}
