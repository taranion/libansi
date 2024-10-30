package org.prelle.ansi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GraphicsRenditionTest {

	private final static char ESC = (char)27;

	//-------------------------------------------------------------------
	public static String ESC(String value) {
		return ESC+value;
	}

	//-------------------------------------------------------------------
	public static String SGR(int n, String value) {
		return ESC+"["+n+"m"+value;
	}

	//-------------------------------------------------------------------
	public static String SGR(int n, int...param) {
		StringBuffer buf = new StringBuffer(ESC+"["+n);
		for (int p : param)
			buf.append(";"+p);
		return buf+"m";
	}

		//-------------------------------------------------------------------
		public static String BG(int color256) {
			return SGR(48,5,color256);
		}

		//-------------------------------------------------------------------
		public static String FG(int color256) {
			return SGR(38,5,color256);
		}

		//-------------------------------------------------------------------
		public static String reset() {
			return SGR(0);
		}
	//-------------------------------------------------------------------
	public static String getVT100GRTest() {
		StringBuffer buf = new StringBuffer();
		buf.append(String.format("%20sGraphic rendition test pattern:\n\n", ""));
		buf.append(String.format("%-36s    "+SGR(1)+"%-36s\n", "vanilla", "bold"+SGR(0)+reset()+"\n"));
		buf.append(String.format("     "+SGR(4)+"underline"+SGR(0)+"%22s         "+SGR(4)+SGR(1)+"bold underline"+SGR(0)+"\n\n", ""));
		buf.append(String.format(SGR(5)+"blink"+SGR(0)+"%-31s    "+SGR(5)+SGR(1)+"bold blink"+SGR(0)+"%-26s"+reset()+"\n\n", "", ""));
		buf.append(String.format("     "+SGR(4)+SGR(5)+"underline blink"+SGR(0)+"%16s         "+SGR(4)+SGR(1)+SGR(5)+"bold underline blink"+SGR(0)+"\n\n", ""));
		buf.append(String.format(SGR(7)+"negative"+SGR(0)+"%-28s    "+SGR(7)+SGR(1)+"bold negative"+SGR(0)+"%-23s"+reset()+"\n\n", "", ""));
		buf.append(String.format("     "+SGR(4)+SGR(7)+"underline negative"+SGR(0)+"%14s         "+SGR(4)+SGR(7)+SGR(1)+"bold underline negative"+SGR(0)+"\n\n", ""));
		buf.append(String.format(SGR(7)+SGR(5)+"blink negative"+SGR(0)+"%-22s    "+SGR(7)+SGR(5)+SGR(1)+"bold blink negative"+SGR(0)+"%-17s"+reset()+"\n\n", "", ""));
		buf.append(String.format("     "+SGR(4)+SGR(5)+SGR(7)+"underline blink negative"+SGR(0)+"%8s         "+SGR(4)+SGR(5)+SGR(7)+SGR(1)+"bold underline blink negative"+SGR(0)+"\n\n", ""));

		return buf.toString();
	}

	//-------------------------------------------------------------------
	public static String COL(int color) {
		int fore = ColorPalette.WHITE;
		if (color>=8  & color<16) fore=ColorPalette.BLACK;

		return BG(color)+FG(fore)+String.format("%-3s", color);
	}

	//-------------------------------------------------------------------
	public static String get256ColorTest() {
		StringBuffer buf = new StringBuffer();
		buf.append("Standard: "+FG(ColorPalette.BRIGHT_WHITE));
		for (int i=0; i<8; i++) buf.append(BG(i)+String.format("%4s ",i)); buf.append(reset()+"\n");
		buf.append("Intense:  "+FG(ColorPalette.BLACK));
		for (int i=8; i<16; i++) buf.append(BG(i)+String.format("%4s ",i)); buf.append(reset()+"\n");
		buf.append("\n");
		for (int line=0; line<18; line++) {
			for (int col=0; col<12; col++) {
				if (col==0) buf.append(FG(ColorPalette.BRIGHT_WHITE));
				if (col==6) buf.append(FG(ColorPalette.BLACK));
				int square = (line/6) + (col/6)*3;
				int color  = 16 + 6*square + (col%6) + (line%6)*36;
//				System.out.println(square + "/"+color);
				buf.append(BG(color)+String.format("%4s ",color));
				if (col==5)
					buf.append(reset()+"   ");
			}
			buf.append(reset()+"\n");
			if (line==5) buf.append("\n");
			if (line==11) buf.append("\n");
			if (line==17) buf.append("\n");
		}
		buf.append("Grays:  "+FG(ColorPalette.BRIGHT_WHITE));
		for (int i=232; i<244; i++) buf.append(BG(i)+String.format("%4s ",i)); buf.append(reset()+"\n");
		buf.append("        "+FG(ColorPalette.BLACK));
		for (int i=244; i<256; i++) buf.append(BG(i)+String.format("%4s ",i)); buf.append(reset()+"\n");

		return buf.toString();
	}

	//-------------------------------------------------------------------
	public static String getFontSizeTest() {
		StringBuffer buf = new StringBuffer();
		buf.append("Double Width Line:\n");
		buf.append("Before: ");
		buf.append(ESC("#6"));
		buf.append("  Within Same Line\n");
		buf.append("  Within Next Line ");
		buf.append(ESC("#5"));
		buf.append("  After Same \n");
		buf.append("  After Next \n");
		buf.append("\nDouble Height Line:\n");
		buf.append(ESC("#3")+"Testline 123\n");
		buf.append(ESC("#4")+"Testline 123\n");
		buf.append(ESC("#5\r\nDone\r\n"));

		return buf.toString();
	}

	//-------------------------------------------------------------------
	public static void main(String[] args) throws IOException {
//		System.out.println(getVT100GRTest());

		File tmp = new File("/tmp/sgr-test.ans");
		FileWriter out = new FileWriter(tmp);
		out.append(getVT100GRTest());
		out.flush();
		out.close();

		System.out.println(get256ColorTest());
		tmp = new File("/tmp/256color.ans");
		out = new FileWriter(tmp);
		out.append(get256ColorTest());
		out.flush();
		out.close();

		System.out.println(getFontSizeTest());
		tmp = new File("/tmp/fontsize.ans");
		out = new FileWriter(tmp);
		out.append(getFontSizeTest());
		out.flush();
		out.close();
//		System.console().writer().append(getVT100GRTest());
	}

}
