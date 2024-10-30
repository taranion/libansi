package org.prelle.ansi;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;
import org.prelle.ansi.commands.DynamicallyRedefinableCharacterSet;
import org.prelle.ansi.commands.SelectGraphicRendition;
import org.prelle.ansi.commands.SelectGraphicRendition.Meaning;
import org.prelle.ansi.commands.Sixel.BackgroundMode;
import org.prelle.ansi.commands.Sixel.SixelData;
import org.prelle.ansi.commands.Sixel;
import org.prelle.ansi.commands.DynamicallyRedefinableCharacterSet.TextOrFullCell;
import org.prelle.ansi.commands.xterm.XtermTextParameter;

public class ANSIOutputTest {

	//-------------------------------------------------------------------
	@Test
	public void test1() throws IOException {
		ANSIOutputStream out = new ANSIOutputStream(System.out);
		out.write("Hallö");
		out.write(0x08);
		out.write("o ");
		out.write(new SelectGraphicRendition(List.of(Meaning.RESET)));
		out.write("Welt");
		out.flush();
		out.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void test2() throws IOException {
		XtermTextParameter setTitle = new XtermTextParameter(XtermTextParameter.WINDOW, "Super");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		setTitle.encode(baos, true);
		byte[] result = baos.toByteArray();
		byte[] expect = new byte[] {0x1B, 0x5D, 0x32, (byte)(int)';', 83, 117, 112, 101, 114, 27, 92};
		assertArrayEquals(expect, result);

		ANSIOutputStream out = new ANSIOutputStream(System.out);
		out.write(setTitle);
		out.flush();
		out.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testSixel() throws IOException {
		Sixel setTitle = new Sixel(9, BackgroundMode.TRANSPARENT, List.of(
				new SixelData("aaaaa")));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		setTitle.encode(baos, true);
		byte[] result = baos.toByteArray();
		byte[] expect = (((char)0x1B)+"P9;1qaaaaa"+((char)0x1B)+"\\").getBytes(StandardCharsets.US_ASCII);
		//byte[] expect = new byte[] {0x1B, 0x50, 0x39, (byte)(int)';', 0x31, 0x71, 97, 97, 97, 97, 97, 0x1B, 0x5C};
		assertArrayEquals(expect, result);

		ANSIOutputStream out = new ANSIOutputStream(System.out);
		out.write(setTitle);
		out.flush();
		out.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testDRCS() throws IOException {
		DynamicallyRedefinableCharacterSet decdld = new DynamicallyRedefinableCharacterSet(
				1,1,DynamicallyRedefinableCharacterSet.Erase.ONLY_RELOADED,0,0,TextOrFullCell.FULL_CELL,0,0,
				"???owYn||~ywo??/?IRJaVNn^NVbJRI");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		decdld.encode(baos, true);
		byte[] result = baos.toByteArray();
		System.out.println("testDRCS "+(new String(result)));
//		byte[] expect = (((char)0x1B)+"P1;1;2{ @ogcacgo/B?????B"+((char)0x1B)+"\\").getBytes(StandardCharsets.US_ASCII);
//		//byte[] expect = new byte[] {0x1B, 0x50, 0x39, (byte)(int)';', 0x31, 0x71, 97, 97, 97, 97, 97, 0x1B, 0x5C};
//		assertArrayEquals(expect, result);

		ANSIOutputStream out = new ANSIOutputStream(System.out);
		out.write(decdld);
		out.flush();
		out.close();
	}

}
