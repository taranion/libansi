package org.prelle.ansi;

import java.io.ByteArrayOutputStream;

/**
 *
 */
public abstract class SequenceFragment extends C1Fragment {

	protected String intermediate;
	protected int finalChar;
	protected Level level;
	protected String commandName;

	//-------------------------------------------------------------------
	protected SequenceFragment(C1Code code) {
		super(code);
	}

	//-------------------------------------------------------------------
	protected SequenceFragment(C1Code code, int finalChar, String name, Level level) {
		super(code);
		this.finalChar = finalChar;
		this.commandName = name;
		this.level   = level;
	}

	//-------------------------------------------------------------------
	protected SequenceFragment(C1Code code, String intermediate, int finalChar, String name, Level level) {
		super(code);
		this.intermediate = intermediate;
		this.finalChar = finalChar;
		this.commandName = name;
		this.level   = level;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.C1Fragment#getName()
	 */
	@Override
	public String getName() {
		return commandName;
	}

	//-------------------------------------------------------------------
	public Level getLevel() {
		return level;
	}

	//-------------------------------------------------------------------
	public int getFinalChar() {
		return finalChar;
	}

	//-------------------------------------------------------------------
	public String toString() {
		StringBuffer ret = new StringBuffer(commandName);
		if (intermediate!=null)
			ret.append(intermediate);
		ret.append( (char)finalChar);
		return ret.toString();
	}

	//-------------------------------------------------------------------
	public String getKey() {
		if (intermediate!=null)
			return intermediate+((char)finalChar);
		return String.valueOf((char)finalChar);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.C1Fragment#encode(java.io.ByteArrayOutputStream, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		super.encode(toFill, use7Bit);

	}

	//-------------------------------------------------------------------
	/**
	 * @return the intermediate
	 */
	public String getIntermediate() {
		return intermediate;
	}

}
