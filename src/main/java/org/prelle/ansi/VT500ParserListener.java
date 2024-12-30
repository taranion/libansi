package org.prelle.ansi;

/**
 *
 */
public interface VT500ParserListener {

	public void execute(C0Code c0);
	public void execute(C1Code c1);

	public void print(byte c);
	public void print(char c);

	public void handleOperatingSystemCommand(String data);
	public void handleEscape(int code, String parameter);
	public void controlSequence(int code, String inter, String param);
	public void handleDeviceControlString(int code, String inter, String param, String data);
	public void handleStringMessage(C1Code valueOf, String data);

}
