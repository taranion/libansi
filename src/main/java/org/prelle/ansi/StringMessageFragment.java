package org.prelle.ansi;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;

/**
 *
 */
public class StringMessageFragment extends C1Fragment {

	protected String data;

	//-------------------------------------------------------------------
	public StringMessageFragment(C1Code code, String data) {
		super(code);
		this.data = data;
	}

	//-------------------------------------------------------------------
	/**
	 * The fixed literal prefix of {@link #data} that identifies this class,
	 * e.g. {@code "25a1;s;"}. Used by {@code AllCommands.parseStringMessage()}
	 * for dispatch, analogous to {@link ControlSequenceFragment#getKey()} for
	 * CSI sequences -- except CSI has a structural final-byte/intermediate
	 * pair to key on before parsing parameters, while APC/OSC/PM bodies are
	 * free-form strings whose "type" is a per-protocol convention, so this is
	 * matched by prefix instead of an exact key. Subclasses that want to be
	 * auto-decoded from incoming messages override this; {@code null} (the
	 * default) means "generic, not auto-decoded".
	 */
	public String getPrefix() {
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * Build a typed instance from an incoming message body, mirroring
	 * {@link ControlSequenceFragment#decode(Class, String)}: constructs a
	 * bare instance via {@code cls}'s no-arg constructor, stores the raw
	 * {@code data}, then calls {@link #decodingHook()} so the subclass can
	 * parse its typed fields out of it.
	 */
	public static <T extends StringMessageFragment> T decode(Class<T> cls, String data) {
		try {
			Constructor<T> cons = cls.getDeclaredConstructor();
			cons.setAccessible(true);
			T instance = cons.newInstance();
			instance.data = data;
			instance.decodingHook();
			return instance;
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed instantiating " + cls, e);
		}
	}

	//-------------------------------------------------------------------
	/** Called from {@link #decode(Class, String)} so subclasses can parse
	 * {@link #data} into their own typed fields right after construction. */
	protected void decodingHook() {
	}

	//-------------------------------------------------------------------
	public String toString() {
		if (data!=null && data.length()>50)
			return code+":"+data.substring(0, 50);
		return code+":"+data;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.SequenceFragment#encode(java.io.ByteArrayOutputStream, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		super.encode(toFill, use7Bit);
		toFill.writeBytes(data.getBytes(StandardCharsets.US_ASCII));
		if (use7Bit) {
			toFill.write((byte) C0Code.ESC.code);
			toFill.write((byte) (C1Code.ST.getAsEscapeCode()));
		} else {
			toFill.write((byte) C1Code.ST.code);
		}
	}

}
