package org.prelle.ansi;

/**
 *
 */
public interface VT500ParserListener {

	public void execute(C0Code c0);
	public void execute(C1Code c1);

	public void print(byte c);
	public void print(int codepoint);

	public void handleOperatingSystemCommand(String data, byte[] buf);
	public void handleEscape(int code, String parameter, byte[] buf);
	public void controlSequence(int code, String inter, String param, byte[] buf);
	public void handleDeviceControlString(int code, String inter, String param, String data, byte[] buf);
	public void handleStringMessage(C1Code valueOf, String data, byte[] buf);

}
