package org.prelle.ansi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;

import org.junit.Test;
import org.prelle.ansi.commands.CursorForward;
import org.prelle.ansi.commands.CursorPosition;
import org.prelle.ansi.commands.DeviceAttributes;
import org.prelle.ansi.commands.EraseInDisplay;
import org.prelle.ansi.commands.SelectGraphicRendition;
import org.prelle.ansi.commands.SetLeftAndRightMargin;
import org.prelle.ansi.commands.kitty.KittyGraphicsFragment;

public class ANSIParserTest {

	//-------------------------------------------------------------------
	@Test
	public void test() throws IOException {
		String text = "Hallö"+((char)0x08)+"o Welt";

		ByteArrayInputStream bins = new ByteArrayInputStream(text.getBytes(Charset.defaultCharset()));
		ANSIInputStream ain = new ANSIInputStream(bins);
		ain.setCollectPrintable(true);
		AParsedElement fragment = ain.readFragment();
		assertNotNull(fragment);
		assertEquals(PrintableFragment.class, fragment.getClass());

		fragment = ain.readFragment();
		assertNotNull(fragment);
		assertEquals(C0Fragment.class, fragment.getClass());

		fragment = ain.readFragment();
		assertNotNull(fragment);
		assertEquals(PrintableFragment.class, fragment.getClass());

		assertNull(ain.readFragment());
		ain.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void test2() throws IOException {
		InputStream in = ClassLoader.getSystemResourceAsStream("screen_ascii.ans");
		ANSIInputStream ain = new ANSIInputStream(in);
		ain.setCollectPrintable(true);
		AParsedElement fragment = ain.readFragment();
		assertNotNull(fragment);
		assertEquals(SelectGraphicRendition.class, fragment.getClass());

		fragment = ain.readFragment();
		assertNotNull(fragment);
		assertEquals(PrintableFragment.class, fragment.getClass());

		fragment = ain.readFragment();
		assertNotNull(fragment);
		assertEquals(SelectGraphicRendition.class, fragment.getClass());

		assertNotNull(ain.readFragment());
	}

	//-------------------------------------------------------------------
	@Test
	public void test3() throws IOException {
		String text = ((char)0x1b)+"[>0c";

		ByteArrayInputStream bins = new ByteArrayInputStream(text.getBytes(StandardCharsets.ISO_8859_1));
		ANSIInputStream ain = new ANSIInputStream(bins);
		AParsedElement fragment = ain.readFragment();
		System.out.println(fragment);
		assertNotNull(fragment);
		assertEquals(DeviceAttributes.class, fragment.getClass());
		ain.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testC1Sequence() throws IOException {
		String text7Bit = ((char)0x1b)+"[12;3s";
		String text8Bit = ((char)0x9b)+"12;3s";

		ByteArrayInputStream bins = new ByteArrayInputStream(text7Bit.getBytes(StandardCharsets.ISO_8859_1));
		ANSIInputStream ain = new ANSIInputStream(bins);
		AParsedElement fragment = ain.readFragment();
		System.out.println(fragment);
		assertNotNull(fragment);
		assertEquals(SetLeftAndRightMargin.class, fragment.getClass());
		ain.close();

		bins = new ByteArrayInputStream(text8Bit.getBytes(StandardCharsets.ISO_8859_1));
		ain = new ANSIInputStream(bins);
		ain.setEncoding(StandardCharsets.ISO_8859_1);
		AParsedElement fragment2 = ain.readFragment();
		System.out.println(fragment2);
		assertNotNull(fragment2);
		assertEquals(SetLeftAndRightMargin.class, fragment.getClass());
		ain.close();

		assertEquals(((C1Fragment)fragment).getCode(), ((C1Fragment)fragment2).getCode());
	}

	//-------------------------------------------------------------------
	@Test
	public void testSequenceIdentity() throws IOException {
		String variation1 = ((char)0x1b)+"[;4;5m";
		String variation2 = ((char)0x1b)+"[m"+((char)0x1b)+"[4m"+((char)0x1b)+"[5m";
		String variation3 = ((char)0x1b)+"[0;04;005m";

		ANSIInputStream ain = new ANSIInputStream(new ByteArrayInputStream(variation1.getBytes(StandardCharsets.US_ASCII)));
		AParsedElement fragment = ain.readFragment();
		System.out.println(fragment);
		assertNotNull(fragment);
		assertEquals(SelectGraphicRendition.class, fragment.getClass());
		assertEquals(List.of(0,4,5) ,((SelectGraphicRendition)fragment).getArguments());
		ain.close();

		ain = new ANSIInputStream(new ByteArrayInputStream(variation2.getBytes(StandardCharsets.ISO_8859_1)));
		AParsedElement fragment2 = ain.readFragment();
		System.out.println(fragment2);
		assertNotNull(fragment2);
		assertEquals(SelectGraphicRendition.class, fragment2.getClass());
//		assertEquals(List.of(0,4,5) ,((SelectGraphicRendition)fragment2).getArguments());
		ain.close();

		ain = new ANSIInputStream(new ByteArrayInputStream(variation3.getBytes(StandardCharsets.ISO_8859_1)));
		AParsedElement fragment3 = ain.readFragment();
		System.out.println(fragment3);
		assertNotNull(fragment3);
		assertEquals(SelectGraphicRendition.class, fragment3.getClass());
		assertEquals(List.of(0,4,5) ,((SelectGraphicRendition)fragment3).getArguments());
		ain.close();


		assertEquals(((C1Fragment)fragment).getCode(), ((C1Fragment)fragment2).getCode());
		assertEquals(((C1Fragment)fragment).getCode(), ((C1Fragment)fragment3).getCode());
	}

	//-------------------------------------------------------------------
	@Test
	public void testDCS() throws IOException {
		String text7Bit = ((char)0x1b)+"P12;3s\u009c";
		String text8Bit = "\u009012;3|F6=17\u009c";

		ByteArrayInputStream bins = new ByteArrayInputStream(text8Bit.getBytes(StandardCharsets.ISO_8859_1));
		ANSIInputStream ain = new ANSIInputStream(bins);
		ain.setEncoding(StandardCharsets.ISO_8859_1);
		AParsedElement fragment = ain.readFragment();
		System.out.println(fragment);
		assertNotNull(fragment);
		assertEquals(DeviceControlFragment.class, fragment.getClass());
		ain.close();

		bins = new ByteArrayInputStream(text7Bit.getBytes(StandardCharsets.ISO_8859_1));
		ain = new ANSIInputStream(bins);
		ain.setEncoding(StandardCharsets.ISO_8859_1);
		AParsedElement fragment2 = ain.readFragment();
		System.out.println(fragment2);
		assertNotNull(fragment2);
		assertEquals(DeviceControlFragment.class, fragment2.getClass());
		ain.close();

		assertEquals(((C1Fragment)fragment).getCode(), ((C1Fragment)fragment2).getCode());
	}

	//-------------------------------------------------------------------
	@Test
	public void testDA() throws IOException {
		String text8Bit = "\u009b?61;1;21;22c";

		ByteArrayInputStream bins = new ByteArrayInputStream(text8Bit.getBytes(StandardCharsets.ISO_8859_1));
		ANSIInputStream ain = new ANSIInputStream(bins);
		ain.setEncoding(StandardCharsets.ISO_8859_1);
		AParsedElement fragment = ain.readFragment();
		System.out.println(fragment);
		assertNotNull(fragment);
		assertEquals(DeviceAttributes.class, fragment.getClass());
		ain.close();

	}

	//-------------------------------------------------------------------
	@Test
	public void testAPC() throws IOException {
		String text8Bit = "\u001b_Gi=31;OK\u009c";

		ByteArrayInputStream bins = new ByteArrayInputStream(text8Bit.getBytes(StandardCharsets.ISO_8859_1));
		ANSIInputStream ain = new ANSIInputStream(bins);
		ain.setEncoding(StandardCharsets.ISO_8859_1);
		AParsedElement fragment = ain.readFragment();
		System.out.println(fragment);
		assertNotNull(fragment);
		assertTrue(fragment instanceof StringMessageFragment);
		assertEquals(KittyGraphicsFragment.class, fragment.getClass());
		ain.close();

	}

	//-------------------------------------------------------------------
	@Test
	public void parseDCS() throws IOException {
		String text8Bit = ((char)C1Code.DCS.code)+"1;1;0;12;1;1;12;0{ @"
				+ "--------/--------/--------;"
				+ "????????/????????/????????;"
				+ "~@@@@@@~/~??????~/~GGGGGG~;"
				+ "TTTTTTTT/TTTTTTTT/TTTTTTTT;"+((char)C1Code.ST.code);

		ByteArrayInputStream bins = new ByteArrayInputStream(text8Bit.getBytes(StandardCharsets.ISO_8859_1));
		ANSIInputStream ain = new ANSIInputStream(bins);
		ain.setEncoding(StandardCharsets.ISO_8859_1);
		AParsedElement fragment = ain.readFragment();
		System.out.println(fragment);
		assertNotNull(fragment);
		assertEquals(DeviceControlFragment.class, fragment.getClass());
		ain.close();

	}

	//-------------------------------------------------------------------
	@Test
	public void testCP437() throws IOException {
		InputStream in = ClassLoader.getSystemResourceAsStream("cp437MUD.ans");
		ANSIInputStream ain = new ANSIInputStream(in);
		ain.setEncoding(Charset.forName("CP437"));
		ain.setCollectPrintable(true);
		AParsedElement fragment = ain.readFragment();
		assertNotNull(fragment);
		assertEquals(SelectGraphicRendition.class, fragment.getClass());

		fragment = ain.readFragment();
		assertNotNull(fragment);
		assertEquals(EraseInDisplay.class, fragment.getClass());
		assertEquals(CursorPosition.class, ain.readFragment().getClass());
		assertEquals(SelectGraphicRendition.class, ain.readFragment().getClass());
		assertEquals(CursorForward.class, ain.readFragment().getClass());
		assertEquals(SelectGraphicRendition.class, ain.readFragment().getClass());
		//assertEquals(PrintableFragment.class, fragment.getClass());

		fragment = ain.readFragment();
		assertNotNull(fragment);
		assertEquals(PrintableFragment.class, fragment.getClass());
		byte[] bytes = ((PrintableFragment)fragment).getData();
		System.out.println("Bytes read: "+HexFormat.of().formatHex(bytes));
		assertEquals(5, ((PrintableFragment)fragment).getText().length());
		assertEquals(15, bytes.length);
	}
}
