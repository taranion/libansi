package org.prelle.ansi.commands.xterm;

import java.util.List;

import org.prelle.ansi.DeviceControlFragment;

/**
 *
 */
public class XTermGetTerminalCap extends DeviceControlFragment {

	public XTermGetTerminalCap(String data) {
		super("+", List.of(), "q"+data);
		// TODO Auto-generated constructor stub
	}

}
