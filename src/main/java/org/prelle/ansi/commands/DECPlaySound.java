package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;
import org.prelle.ansi.commands.EraseInDisplay.Mode;

/**
 * https://tightloop.io/terminal-decps/
 */
public class DECPlaySound extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public DECPlaySound() {
		super(",", '~', "DECPS", Level.VT500);
	}

	//-------------------------------------------------------------------
	/**
	 * @param volume  The volume is an integer within the range [0, 7] where - 0 is muted - 1..3 is low volume - 4..7 is high volume
	 * @param duration The duration of the note in 1/32 parts of a second. So 32 is 1 second.
	 * @param note    The register of notes is represented with the interval [1, 25] and ranges from C5 to a C7. The specification specifies a few example frequencies, but they appear to be incorrect.
	 */
	public DECPlaySound(int volume, int duration, int note) {
		this();
		parameter.clear();
		parameter.add(volume);
		parameter.add(duration);
		parameter.add(note);
	}

	//-------------------------------------------------------------------
	public Mode getValue() {
		int value = parameter.get(0);
		return Mode.valueOf(value);
	}

}
