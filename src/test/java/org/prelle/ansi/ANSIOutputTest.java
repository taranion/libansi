package org.prelle.ansi;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.prelle.ansi.commands.DynamicallyRedefinableCharacterSet;
import org.prelle.ansi.commands.DynamicallyRedefinableCharacterSet.TextOrFullCell;
import org.prelle.ansi.commands.SelectGraphicRendition;
import org.prelle.ansi.commands.SelectGraphicRendition.Meaning;
import org.prelle.ansi.commands.Sixel;
import org.prelle.ansi.commands.Sixel.BackgroundMode;
import org.prelle.ansi.commands.Sixel.SixelData;
import org.prelle.ansi.commands.xterm.XtermTextParameter;

public class ANSIOutputTest {

	//-------------------------------------------------------------------
	@Test
	public void test1() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ANSIOutputStream out = new ANSIOutputStream(baos);
		out.write("Hallö");
		out.write(0x08);
		out.write("o ");
		out.write(new SelectGraphicRendition(List.of(Meaning.RESET)));
		out.write("Welt");
		out.flush();
		out.close();

		byte[] expect = "Hallö\bo \u001b[0mWelt".getBytes(StandardCharsets.UTF_8);
		assertArrayEquals(expect, baos.toByteArray());
	}

	//-------------------------------------------------------------------
	@Test
	public void test2() throws IOException {
		XtermTextParameter setTitle = new XtermTextParameter(XtermTextParameter.WINDOW, "Super");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		setTitle.encode(baos, true);
		byte[] result = baos.toByteArray();
		byte[] expect = new byte[] {0x1B, 0x5D, 0x32, (byte)';', 83, 117, 112, 101, 114, 27, 92};
		assertArrayEquals(expect, result);

		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
		ANSIOutputStream out = new ANSIOutputStream(baos2);
		out.write(setTitle);
		out.flush();
		out.close();
		assertArrayEquals(expect, baos2.toByteArray());
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
		assertArrayEquals(expect, result);

		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
		ANSIOutputStream out = new ANSIOutputStream(baos2);
		out.write(setTitle);
		out.flush();
		out.close();
		assertArrayEquals(expect, baos2.toByteArray());
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

		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
		ANSIOutputStream out = new ANSIOutputStream(baos2);
		out.write(decdld);
		out.flush();
		out.close();
		assertArrayEquals(result, baos2.toByteArray());
	}

	//-------------------------------------------------------------------
	@Test
	public void testWriteUTF8String() throws IOException {
		String text = "Was ist äöüß? oder ╔═╗║╚╝║─╤╧╟╢┼┴┬┤├ ?\r\nKein Emoji 😀!";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ANSIOutputStream out = new ANSIOutputStream(baos);
		assertTrue(out.isUtf8Mode());

		out.write(text);
		out.flush();
		out.close();

		byte[] expect = text.getBytes(StandardCharsets.UTF_8);
		assertArrayEquals(expect, baos.toByteArray());
	}

	//-------------------------------------------------------------------
	@Test
	public void testWriteISO88591String() throws IOException {
		String text = "Was ist äöüß?";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ANSIOutputStream out = new ANSIOutputStream(baos);
		out.setUtf8Mode(false);

		out.write(text);
		out.flush();
		out.close();

		byte[] expect = text.getBytes(StandardCharsets.ISO_8859_1);
		assertArrayEquals(expect, baos.toByteArray());
	}

	//-------------------------------------------------------------------
	@Test
	public void testWritePrintableFragmentUTF8() throws IOException {
		String text = "Title: ╔═╗║╚╝";
		PrintableFragment frag = new PrintableFragment(text);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ANSIOutputStream out = new ANSIOutputStream(baos);
		out.write(frag);
		out.flush();
		out.close();

		byte[] expect = text.getBytes(StandardCharsets.UTF_8);
		assertArrayEquals(expect, baos.toByteArray());
	}

	//-------------------------------------------------------------------
	@Test
	public void testWriteC0AndC1Fragments() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ANSIOutputStream out = new ANSIOutputStream(baos);

		out.write(new C0Fragment(C0Code.CR));
		out.write(new C0Fragment(C0Code.LF));
		out.write(new C1Fragment(C1Code.CSI));
		out.flush();
		out.close();

		// C0 CR=0x0D, LF=0x0A, 7-bit C1 CSI = ESC [ (0x1B 0x5B)
		byte[] expect = new byte[] {0x0D, 0x0A, 0x1B, 0x5B};
		assertArrayEquals(expect, baos.toByteArray());
	}

	//-------------------------------------------------------------------
	@Test
	public void testSetTextColorAndReset() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ANSIOutputStream out = new ANSIOutputStream(baos);

		out.setTextColor(10);
		out.reset();
		out.flush();
		out.close();

		// setTextColor(10) -> ESC [ 3 8 ; 5 ; 1 0 m
		// reset() -> ESC [ 0 m
		String result = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		assertEquals("\u001b[38;2;38;5;10m\u001b[0m", result);
	}

	//-------------------------------------------------------------------
	@Test
	public void testLoggingListener() throws IOException {
		List<String> logs = new ArrayList<>();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ANSIOutputStream out = new ANSIOutputStream(baos);
		out.setLoggingListener((name, val) -> logs.add(name + "=" + val));

		out.write(new C0Fragment(C0Code.LF));
		out.write(new SelectGraphicRendition(31));

		assertEquals(2, logs.size());
		assertEquals("LF=C0(LF)", logs.get(0));
		assertTrue(logs.get(1).startsWith("SGR="));

		out.close();
	}
}
