package org.prelle.ansi.commands;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prelle.ansi.C1Code;
import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.DeviceControlFragment;
import org.prelle.ansi.EscapeSequenceFragment;
import org.prelle.ansi.StringMessageFragment;
import org.prelle.ansi.commands.kitty.KittyGraphicsFragment;
import org.prelle.ansi.commands.rio.GlyphClearFragment;
import org.prelle.ansi.commands.rio.GlyphQueryFragment;
import org.prelle.ansi.commands.rio.GlyphRegisterFragment;
import org.prelle.ansi.commands.rio.GlyphSupportFragment;
import org.prelle.ansi.commands.xterm.XTermWindowOperation;

/**
 * Registry and factory for parsing ANSI/VT500 control sequences, escape sequences, and device control sequences.
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
			
			QueryRIPScrip.class,
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

	/**
	 * Typed replies for APC/OSC/PM ("string message") bodies. Unlike CSI,
	 * these have no structural key to look up before parsing -- each class
	 * declares a literal {@link StringMessageFragment#getPrefix()} instead,
	 * matched by longest-prefix (see {@link #parseStringMessage(C1Code, String)}).
	 */
	@SuppressWarnings("unchecked")
	public final static Class<? extends StringMessageFragment>[] STRING_MESSAGES = new Class[]{
			KittyGraphicsFragment.class,
			GlyphSupportFragment.class,
			GlyphQueryFragment.class,
			GlyphRegisterFragment.class,
			GlyphClearFragment.class,
	};

	private static Map<String,Class<? extends ControlSequenceFragment>> csiByCode;
	private static Map<C1Code,List<Class<? extends StringMessageFragment>>> stringMsgByCode;
	private static Map<Class<? extends StringMessageFragment>,String> stringMsgPrefix;

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

	static {
		stringMsgByCode = new HashMap<>();
		stringMsgPrefix = new HashMap<>();
		for (Class<? extends StringMessageFragment> cls : STRING_MESSAGES) {
			try {
				cls.getDeclaredConstructor().setAccessible(true);
				StringMessageFragment proto = cls.getDeclaredConstructor().newInstance();
				String prefix = proto.getPrefix();
				if (prefix == null) {
					logger.log(Level.ERROR, "{0} is registered in STRING_MESSAGES but has no getPrefix()", cls);
					continue;
				}
				stringMsgPrefix.put(cls, prefix);
				stringMsgByCode.computeIfAbsent(proto.getCode(), k -> new ArrayList<>()).add(cls);
			} catch (NoSuchMethodException nsm) {
				logger.log(Level.ERROR, nsm.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//-------------------------------------------------------------------
	/**
	 * Decode an incoming APC/OSC/PM message body into a typed subclass of
	 * {@link StringMessageFragment}, if one is registered in
	 * {@link #STRING_MESSAGES} for this {@link C1Code} whose
	 * {@link StringMessageFragment#getPrefix()} matches -- longest prefix
	 * wins if more than one would match. Falls back to a plain, undecoded
	 * {@link StringMessageFragment} if nothing matches, so an unrecognised
	 * message is never lost or rejected (mirroring how APC/OSC/PM are meant
	 * to be safely ignorable by consumers that don't know them).
	 */
	public static StringMessageFragment parseStringMessage(C1Code code, String data) {
		List<Class<? extends StringMessageFragment>> candidates = stringMsgByCode.get(code);
		if (candidates != null && data != null) {
			Class<? extends StringMessageFragment> best = null;
			int bestLen = -1;
			for (Class<? extends StringMessageFragment> cls : candidates) {
				String prefix = stringMsgPrefix.get(cls);
				if (data.startsWith(prefix) && prefix.length() > bestLen) {
					best = cls;
					bestLen = prefix.length();
				}
			}
			if (best != null) {
				return StringMessageFragment.decode(best, data);
			}
		}
		return new StringMessageFragment(code, data);
	}

	//-------------------------------------------------------------------
	public static ControlSequenceFragment parseControlSequence(int code, String intermediate, String paramString) {
		String key = (intermediate!=null)?(intermediate+(char)code):String.valueOf( (char)code);

//		logger.log(Level.ERROR, "Finding ''{0}'' in csiCode returns {1}", key, csiByCode.get(key));
//		logger.log(Level.ERROR, "... ", csiByCode.keySet().stream().filter(k -> k.charAt(0)=='!').toList());
//		csiByCode.keySet().stream().forEachOrdered(x -> System.out.println(x+"="+csiByCode.get(x)));
		
		Class<? extends ControlSequenceFragment> clazz = csiByCode.get(key);
		if (clazz==null) {
			logger.log(Level.ERROR, "Unsupported CSI command "+Integer.toHexString(code)+" / "+key);
			logger.log(Level.ERROR, "Finding ''{0}'' in csiCode returns {1}", key, csiByCode.get(key));
			logger.log(Level.ERROR, "... ", csiByCode.keySet().stream().filter(k -> k.charAt(0)=='!').toList());
			csiByCode.keySet().stream().forEachOrdered(x -> System.out.println(x+"="+csiByCode.get(x)));
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
