package org.prelle.ansi.commands;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Map;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.DeviceControlFragment;
import org.prelle.ansi.EscapeSequenceFragment;
import org.prelle.ansi.commands.kitty.KittyScaledFontFragment;
import org.prelle.ansi.commands.xterm.XTermWindowOperation;

/**
 *
 */
public class AllCommands {

	private final static Logger logger = System.getLogger(AllCommands.class.getPackageName());

	@SuppressWarnings("unchecked")
	public final static Class<? extends ControlSequenceFragment>[] CONTROL_SEQUENCES = new Class[]{
			// ANSI
			CursorBackwardTabulation.class, // CBT
			CursorHorizontalAbsolute.class, // CHA
			CursorPositionReport.class, // CPR
			CursorBackward.class, // CUB
			CursorDown.class,     // CUD
			CursorForward.class,  // CUF
			CursorPosition.class, // CUP
			CursorUp.class,       // CUU
			CursorVerticalTabulation.class, // CVT
			DeviceAttributes.class,
			// TODO: DeleteCharacter DCH
			// TODO: DeleteLine   DL
			DeviceStatusReport.class, // DSR
			// TODO: DimensionTextArea  DTA
			EraseInArea.class,    // EA
			EraseCharacter.class, // ECH
			EraseInDisplay.class, // ED
			EraseInLine.class,    // EL
			FontSelection.class,  // FNT
			GraphicCharacterCombination.class, // GCC
			GraphicSizeModification.class, // GSM
			GraphicSizeSelection.class, // GSS
			HorizontalPositionAbsolute.class, // HPA
			HorizontalPositionBackward.class, // HPB
			HorizontalPositionForward.class, // HPR
			HorizontalAndVerticalPosition.class,
			// InsertCharacter  ICH
			// InsertLine       IL
			// Justify          JFY
			ResetMode.class,    // RM
			// SelectCharacterOrientation SCO
			// SelectCharacterPath   SCP
			// SetCharacterSpacing   SCS
			// ScrollDown
			SelectGraphicRendition.class, // SGR
			SetMode.class, // SM

			// VT100
			SetTopAndBottomMargin.class, // DECSTBM
			MouseButtonPress.class,

			LeftRightMarginMode.class,
			SetLeftAndRightMargin.class,
			// VT200
			// VT400
			ChangeAttributesRectangularArea.class,
			DECCopyRectangularArea.class,
			DECSelectActiveStatusDisplay.class,
			DECSelectStatusDisplayType.class,
			EraseRectangularArea.class,
			FillRectangularArea.class,
			// VT500
			DECPlaySound.class,

			PanDown.class,
			PanUp.class,
			
			
			// Xterm
			XTermWindowOperation.class,
			MXPLine.class,
	};

	@SuppressWarnings("unchecked")
	public final static Class<? extends EscapeSequenceFragment>[] ESCAPE_SEQUENCES = new Class[]{
			DesignateCharacterSet.class,
			DECFunctionKey.class,
			DisableManualInput.class

	};

	@SuppressWarnings("unchecked")
	public final static Class<? extends DeviceControlFragment>[] DCS_SEQUENCES = new Class[]{
			DynamicallyRedefinableCharacterSet.class,
			Sixel.class

	};

	private static Map<String,Class<? extends ControlSequenceFragment>> csiByCode;

	static {
		csiByCode = new HashMap<>();
		for (Class<? extends ControlSequenceFragment> cls : CONTROL_SEQUENCES) {
			try {
				cls.getDeclaredConstructor().setAccessible(true);
				ControlSequenceFragment seq = cls.getDeclaredConstructor().newInstance();
				csiByCode.put(seq.getKey(), cls);
//				logger.log(Level.INFO, "Added {0} / {3} /{1} with {2}", seq.getKey(), seq.getName(), cls.getSimpleName(), Integer.toHexString(seq.getFinalChar()));
			} catch (NoSuchMethodException nsm) {
				logger.log(Level.ERROR, nsm.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	//-------------------------------------------------------------------
	public static ControlSequenceFragment parseControlSequence(int code, String intermediate, String paramString) {
		String key = (intermediate!=null)?(intermediate+(char)code):String.valueOf( (char)code);

		Class<? extends ControlSequenceFragment> clazz = csiByCode.get(key);
		if (clazz==null) {
			logger.log(Level.ERROR, "Unsupported CSI command "+Integer.toHexString(code)+" / "+key);
			return null;
		}

//		logger.log(Level.WARNING, "Call decode() to instantiate "+clazz);
		return ControlSequenceFragment.decode(clazz, paramString);
	}

	//-------------------------------------------------------------------
	public static DeviceControlFragment parseDeviceControlSequence(int code, String intermediate, String paramString, String data) {
		return DeviceControlFragment.decode(intermediate, code, paramString, data);
	}
}
