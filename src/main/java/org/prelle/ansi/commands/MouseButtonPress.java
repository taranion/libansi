package org.prelle.ansi.commands;

import java.io.IOException;
import java.io.InputStream;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * 
 */
public class MouseButtonPress extends ControlSequenceFragment {
	
	

	//-------------------------------------------------------------------
	public MouseButtonPress() {
		super(0x4d, "MB", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public int getButton() {
		return parameter.get(0);
	}

	//-------------------------------------------------------------------
	public int getX() {
		return parameter.get(1);
	}

	//-------------------------------------------------------------------
	public int getY() {
		return parameter.get(2);
	}

	//-------------------------------------------------------------------
	public String toString() {
		return "MousePress("+parameter+")";
	}
	

	//-------------------------------------------------------------------
	public void readExpectedLateBytes(InputStream in) {
		parameter.clear();
		try {
			parameter.add(in.read()-32);
			parameter.add(in.read()-32);
			parameter.add(in.read()-32);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
