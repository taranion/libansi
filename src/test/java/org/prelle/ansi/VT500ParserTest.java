package org.prelle.ansi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.prelle.ansi.commands.AllCommands;
import org.prelle.ansi.commands.DeviceAttributes;
import org.prelle.ansi.commands.DeviceAttributes.Variant;
import org.prelle.ansi.commands.SelectGraphicRendition;

public class VT500ParserTest {

//	//-------------------------------------------------------------------
//	private static void perform(byte[] data) {
//		ParsingStream
//		for (byte b : data) {
//			int code=(b<0)?(256+b):b;
//			parser.parse(code);
//		}
//	}
//
//	//-------------------------------------------------------------------
//	private static void perform(String data) {
//		perform(data.getBytes(Charset.defaultCharset()));
//	}

	//-------------------------------------------------------------------
	@Test
	public void test() throws IOException {
		String text = "Hallö"+((char)0x08)+"o Welt";

		ParsingStream ain = new ParsingStream(text.getBytes(StandardCharsets.ISO_8859_1));
		for (int i=0; i<5; i++) {
			AParsedElement fragment = ain.readFragment();
			assertNotNull(fragment);
			assertEquals(PrintableFragment.class, fragment.getClass());
		}

		AParsedElement fragment = ain.readFragment();
		assertNotNull(fragment);
		assertEquals(C0Fragment.class, fragment.getClass());

		for (int i=0; i<6; i++) {
			fragment = ain.readFragment();
			assertNotNull(fragment);
			assertEquals(PrintableFragment.class, fragment.getClass());
		}

		assertNull(ain.readFragment());
	}

	//-------------------------------------------------------------------
	@Test
	public void test2() throws IOException {
		InputStream in = ClassLoader.getSystemResourceAsStream("screen_ascii.ans");
		byte[] data = in.readAllBytes();
		ParsingStream ain = new ParsingStream(data);
		AParsedElement fragment = ain.readFragment();
		assertNotNull(fragment);
		assertEquals(SelectGraphicRendition.class, fragment.getClass());

		fragment = ain.readFragment();
		assertNotNull(fragment);
		assertEquals(PrintableFragment.class, fragment.getClass());

		fragment = ain.readFragment();
		assertNotNull(fragment);
		assertEquals(PrintableFragment.class, fragment.getClass());

		assertNotNull(ain.readFragment());
	}

	//-------------------------------------------------------------------
	@Test
	public void test3() throws IOException {
		String text = ((char)0x1b)+"[>0c";

		ParsingStream ain = new ParsingStream(text.getBytes(StandardCharsets.ISO_8859_1));
		AParsedElement fragment = ain.readFragment();
		System.out.println(fragment);
		assertNotNull(fragment);
		assertEquals(DeviceAttributes.class, fragment.getClass());
	}

	//-------------------------------------------------------------------
	@Test
	public void testPrimaryDeviceAttributes() throws IOException {
		String text = ((char)0x1b)+"[?61;1;21;22c";

		ParsingStream ain = new ParsingStream(text.getBytes(StandardCharsets.ISO_8859_1));
		AParsedElement fragment = ain.readFragment();
		System.out.println(fragment);
		assertNotNull(fragment);
		assertEquals(DeviceAttributes.class, fragment.getClass());
		DeviceAttributes toEncode = (DeviceAttributes)fragment;
		assertEquals(Variant.Primary_Response, toEncode.getVariant());
	}

	//-------------------------------------------------------------------
	@Test
	public void testSecondaryDeviceAttributes() throws IOException {
		String text = ((char)0x1b)+"[>0c";

		ParsingStream ain = new ParsingStream(text.getBytes(StandardCharsets.ISO_8859_1));
		AParsedElement fragment = ain.readFragment();
		System.out.println(fragment);
		assertNotNull(fragment);
		assertEquals(DeviceAttributes.class, fragment.getClass());
		DeviceAttributes toEncode = (DeviceAttributes)fragment;
		assertEquals(Variant.Secondary, toEncode.getVariant());

		// Now encode
		ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
		toEncode.encode(baos, true);
		byte[] encoded = baos.toByteArray();
		System.out.println(new String(encoded));
		baos.close();

		text = ((char)0x1b)+"[?3;1;2;4;6;9;15;16;22;28c";
		ain = new ParsingStream(text.getBytes(StandardCharsets.ISO_8859_1));
		fragment = ain.readFragment();
		System.out.println(fragment);
		assertNotNull(fragment);
	}

//	//-------------------------------------------------------------------
//	@Test
//	public void testC1Sequence() throws IOException {
//		String text7Bit = ((char)0x1b)+"[12;3s";
//		String text8Bit = ((char)0x9b)+"12;3s";
//
//		ByteArrayInputStream bins = new ByteArrayInputStream(text7Bit.getBytes(StandardCharsets.ISO_8859_1));
//		ANSIInputStream ain = new ANSIInputStream(bins);
//		AParsedElement fragment = ain.readFragment();
//		System.out.println(fragment);
//		assertNotNull(fragment);
//		assertEquals(ControlSequenceFragment.class, fragment.getClass());
//		ain.close();
//
//		bins = new ByteArrayInputStream(text8Bit.getBytes(StandardCharsets.ISO_8859_1));
//		ain = new ANSIInputStream(bins);
//		AParsedElement fragment2 = ain.readFragment();
//		System.out.println(fragment2);
//		assertNotNull(fragment2);
//		assertEquals(ControlSequenceFragment.class, fragment.getClass());
//		ain.close();
//
//		assertEquals(((C1Fragment)fragment).getCode(), ((C1Fragment)fragment2).getCode());
//	}
//
//	//-------------------------------------------------------------------
//	@Test
//	public void testSequenceIdentity() throws IOException {
//		String variation1 = ((char)0x1b)+"[;4;5m";
//		String variation2 = ((char)0x1b)+"[m"+((char)0x1b)+"[4m"+((char)0x1b)+"[5m";
//		String variation3 = ((char)0x1b)+"[0;04;005m";
//
//		ANSIInputStream ain = new ANSIInputStream(new ByteArrayInputStream(variation1.getBytes(StandardCharsets.US_ASCII)));
//		AParsedElement fragment = ain.readFragment();
//		System.out.println(fragment);
//		assertNotNull(fragment);
//		assertEquals(ControlSequenceFragment.class, fragment.getClass());
//		ain.close();
//
//		ain = new ANSIInputStream(new ByteArrayInputStream(variation2.getBytes(StandardCharsets.ISO_8859_1)));
//		AParsedElement fragment2 = ain.readFragment();
//		System.out.println(fragment2);
//		assertNotNull(fragment2);
//		assertEquals(ControlSequenceFragment.class, fragment2.getClass());
//		ain.close();
//
//		ain = new ANSIInputStream(new ByteArrayInputStream(variation3.getBytes(StandardCharsets.ISO_8859_1)));
//		AParsedElement fragment3 = ain.readFragment();
//		System.out.println(fragment3);
//		assertNotNull(fragment3);
//		assertEquals(ControlSequenceFragment.class, fragment3.getClass());
//		ain.close();
//
//
//		assertEquals(((C1Fragment)fragment).getCode(), ((C1Fragment)fragment2).getCode());
//		assertEquals(((C1Fragment)fragment).getCode(), ((C1Fragment)fragment3).getCode());
//	}
//
//	//-------------------------------------------------------------------
//	@Test
//	public void testDCS() throws IOException {
//		String text7Bit = ((char)0x1b)+"[12;3s";
//		String text8Bit = "\u009012;3|F6=17\u009c";
//
//		ByteArrayInputStream bins = new ByteArrayInputStream(text7Bit.getBytes(StandardCharsets.ISO_8859_1));
//		ANSIInputStream ain = new ANSIInputStream(bins);
//		AParsedElement fragment = ain.readFragment();
//		System.out.println(fragment);
//		assertNotNull(fragment);
//		assertEquals(ControlSequenceFragment.class, fragment.getClass());
//		ain.close();
//
//		bins = new ByteArrayInputStream(text8Bit.getBytes(StandardCharsets.ISO_8859_1));
//		ain = new ANSIInputStream(bins);
//		AParsedElement fragment2 = ain.readFragment();
//		System.out.println(fragment2);
//		assertNotNull(fragment2);
//		assertEquals(ControlSequenceFragment.class, fragment.getClass());
//		ain.close();
//
//		assertEquals(((C1Fragment)fragment).getCode(), ((C1Fragment)fragment2).getCode());
//	}
//
//	//-------------------------------------------------------------------
//	@Test
//	public void testDA() throws IOException {
//		String text8Bit = "\u009b?61;1;21;22c";
//
//		ByteArrayInputStream bins = new ByteArrayInputStream(text8Bit.getBytes(StandardCharsets.ISO_8859_1));
//		ANSIInputStream ain = new ANSIInputStream(bins);
//		AParsedElement fragment = ain.readFragment();
//		System.out.println(fragment);
//		assertNotNull(fragment);
//		assertEquals(ControlSequenceFragment.class, fragment.getClass());
//		ain.close();
//
//	}

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
		assertEquals(StringMessageFragment.class, fragment.getClass());
		ain.close();

	}

//	//-------------------------------------------------------------------
//	@Test
//	public void parseDCS() throws IOException {
//		String text8Bit = ((char)C1Code.DCS.code)+"1;1;0;12;1;1;12;0{ @"
//				+ "--------/--------/--------;"
//				+ "????????/????????/????????;"
//				+ "~@@@@@@~/~??????~/~GGGGGG~;"
//				+ "TTTTTTTT/TTTTTTTT/TTTTTTTT;"+((char)C1Code.ST.code);
//
//		ByteArrayInputStream bins = new ByteArrayInputStream(text8Bit.getBytes(StandardCharsets.ISO_8859_1));
//		ANSIInputStream ain = new ANSIInputStream(bins);
//		AParsedElement fragment = ain.readFragment();
//		System.out.println(fragment);
//		assertNotNull(fragment);
//		assertEquals(DeviceControlFragment.class, fragment.getClass());
//		ain.close();
//
//	}

}


class ParsingStream  {

	private List<AParsedElement> stash = new ArrayList<>();

	private byte[] data;
	private int readPos;
	private VT500Parser parser;
	public ParsingStream(byte[] data) {
		this.data = data;

		VT500ParserListener callback = new VT500ParserListener() {

			@Override public void print(byte c) {
				stash.add(new PrintableFragment().add( (char)c));
			}
			@Override public void print(int c) {
				stash.add(new PrintableFragment().add( c));
			}

			@Override
			public void handleOperatingSystemCommand(String data, byte[] buf) {
				System.out.println("handleOperatingSystemCommand "+data);

			}

			@Override
			public void handleEscape(int code, String parameter, byte[] buf) {
				System.out.println("handleEscape "+code+" with '"+parameter+"'");

			}

			@Override
			public void execute(C1Code c1) {
				System.out.println("Execute C1: "+c1);
				stash.add(new C1Fragment(c1));
			}

			@Override
			public void execute(C0Code c0) {
				System.out.println("Execute C0: "+c0);
				stash.add(new C0Fragment(c0));
			}

			@Override
			public void controlSequence(int code, String inter, String param, byte[] buf) {
				System.out.println("Execute CSI: "+code+" with i='"+inter+"' and p='"+param+"'");

				ControlSequenceFragment seq = AllCommands.parseControlSequence(code, inter, param);
				if (seq==null) {
					System.err.println("No control sequence for "+code);
				} else
					stash.add(seq);
			}

			@Override
			public void handleDeviceControlString(int code, String inter, String param, String data, byte[] buf) {
				System.out.println("ToDo: implement handleDeviceControlString: i="+inter+", p="+param+", d="+data);
			}
			@Override
			public void handleStringMessage(C1Code code, String data, byte[] buf) {
				stash.add(new StringMessageFragment(code, data));
			}
		};
		parser = new VT500Parser(callback);
		parser.setEncoding(StandardCharsets.US_ASCII);
	}

	//-------------------------------------------------------------------
	public AParsedElement readFragment() {
		if (!stash.isEmpty()) {
			return stash.remove(0);
		}
		if (readPos>=data.length)
			return null;
		while (stash.isEmpty()) {
			if (readPos>=data.length) return null;
			int code = data[readPos++];
			code = (code<0)?(256+code):code;
			parser.parse(code);
		}
		return stash.remove(0);
	}
}