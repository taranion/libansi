package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.DeviceAttributes.OperatingLevel;
import org.prelle.ansi.Level;

/**
 * With a parameter value not equal to 0, DA is used to identify the device which sends the DA. The
parameter value is a device type identification code according to a register which is to be established. If
the parameter value is 0, DA is used to request an identifying DA from a device.
 */
public class DeviceAttributes extends ControlSequenceFragment {

	public static enum Variant {
		Primary,
		Primary_Response,
		Secondary,
		Tertiary
	}

	private Variant variant = Variant.Primary;
	private org.prelle.ansi.DeviceAttributes.OperatingLevel value = OperatingLevel.SEND_REPORT;

	//-------------------------------------------------------------------
	DeviceAttributes() {
		super(0x63, "DA", Level.VT100);
	}

	//-------------------------------------------------------------------
	/**
	 * Request a specific reqport
	 */
	public DeviceAttributes(Variant variant) {
		this();
		this.variant = variant;
		switch (variant) {
		case Primary :
			firstParam=0;
			break;
		case Secondary:
			firstParam='>';
			break;
		case Tertiary:
			firstParam='=';
			break;
		}
	}

	//-------------------------------------------------------------------
	public Variant getVariant() {
		return variant;
	}

	//-------------------------------------------------------------------
	/** Called from decode() to allow child classes special treatment
	 */
	protected void decodingHook() {
		if (firstParam!=0) {
			if (firstParam=='?') variant=Variant.Primary_Response;
			if (firstParam=='>') variant=Variant.Secondary;
			if (firstParam=='=') variant=Variant.Tertiary;
		} else
			variant = Variant.Primary;
	}

}
