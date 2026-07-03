package org.prelle.ansi;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import org.junit.Test;

public class ANSIInputStreamTest {

	//-------------------------------------------------------------------
	@Test
	public void testPlain7Bit() throws IOException {
		byte[] buf = new byte[] {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64, 0x21};
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);

		ANSIInputStream in = new ANSIInputStream(bais);
		AParsedElement frag = in.readFragment();
		assertNotNull(frag);
		assertTrue(frag instanceof PrintableFragment);
		PrintableFragment print = (PrintableFragment)frag;
		assertEquals("Hello World!", print.getText());
		assertArrayEquals(buf, print.getData());
		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testCarriageReturn7Bit() throws IOException {
		byte[] buf = new byte[] {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x0D, 0x57, 0x6f, 0x72, 0x6c, 0x64, 0x21};
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);

		ANSIInputStream in = new ANSIInputStream(bais);
		AParsedElement frag = in.readFragment();
		assertNotNull(frag);
		assertTrue(frag instanceof PrintableFragment);
		PrintableFragment print = (PrintableFragment)frag;
		assertEquals("Hello", print.getText());

		frag = in.readFragment();
		assertNotNull(frag);
		assertEquals(AParsedElement.Type.C0, frag.type);
		assertTrue(frag instanceof C0Fragment);
		assertEquals(C0Code.CR, ((C0Fragment)frag).getCode());

		frag = in.readFragment();
		assertNotNull(frag);
		assertEquals(AParsedElement.Type.PRINTABLE, frag.type);
		assertTrue(frag instanceof PrintableFragment);
		print = (PrintableFragment)frag;
		assertEquals("World!", print.getText());
		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testTextWithSGR7Bit() throws IOException {
		byte[] buf = new byte[] {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20,
				0x1B, 0x5B, 0x33, 0x33, 0x6D,
				0x57, 0x6f, 0x72, 0x6c, 0x64, 0x21};
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);

		ANSIInputStream in = new ANSIInputStream(bais);
		AParsedElement frag = in.readFragment();
		assertNotNull(frag);
		assertTrue(frag instanceof PrintableFragment);
		PrintableFragment print = (PrintableFragment)frag;
		assertEquals("Hello ", print.getText());

		frag = in.readFragment();
		assertNotNull(frag);
		assertEquals(AParsedElement.Type.C1, frag.type);
		assertTrue(frag instanceof C1Fragment);
		assertEquals(C1Code.CSI, ((C1Fragment)frag).getCode());

		frag = in.readFragment();
		assertNotNull(frag);
		assertEquals(AParsedElement.Type.PRINTABLE, frag.type);
		assertTrue(frag instanceof PrintableFragment);
		print = (PrintableFragment)frag;
		assertEquals("World!", print.getText());
		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testTextWithSGR8Bit() throws IOException {
		byte[] buf = new byte[] {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20,
				(byte) 0x9B, 0x33, 0x33, 0x6D,
				0x57, 0x6f, 0x72, 0x6c, 0x64, 0x21};
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);

		ANSIInputStream in = new ANSIInputStream(bais);
		in.setEncoding(StandardCharsets.US_ASCII);
		AParsedElement frag = in.readFragment();
		assertNotNull(frag);
		assertTrue(frag instanceof PrintableFragment);
		PrintableFragment print = (PrintableFragment)frag;
		assertEquals("Hello ", print.getText());

		frag = in.readFragment();
		assertNotNull(frag);
		assertEquals(AParsedElement.Type.C1, frag.type);
		assertTrue(frag instanceof C1Fragment);
		assertEquals(C1Code.CSI, ((C1Fragment)frag).getCode());

		frag = in.readFragment();
		assertNotNull(frag);
		assertEquals(AParsedElement.Type.PRINTABLE, frag.type);
		assertTrue(frag instanceof PrintableFragment);
		print = (PrintableFragment)frag;
		assertEquals("World!", print.getText());
		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testUTF8Text() throws IOException {
//		String input = "Hä";
//		byte[] comp = input.getBytes(StandardCharsets.UTF_8);
		byte[] buf = new byte[] {0x48, (byte)0xC3, (byte)0xA4};

		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ANSIInputStream in = new ANSIInputStream(bais);
		in.setEncoding(StandardCharsets.UTF_8);
		AParsedElement frag = in.readFragment();
		assertNotNull(frag);
		assertTrue(frag instanceof PrintableFragment);
		PrintableFragment print = (PrintableFragment)frag;
		assertEquals("Hä", print.getText());
		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testUTF8WithC1() throws IOException {
		String input = "Hä\u009B33m";
		byte[] comp = input.getBytes(StandardCharsets.UTF_8);
		byte[] buf = new byte[] {0x48, (byte)0xC3, (byte)0xA4, (byte)0xC2, (byte)0x9B, 0x33, 0x33, 0x6D};

		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ANSIInputStream in = new ANSIInputStream(bais);
		in.setEncoding(StandardCharsets.UTF_8);
		AParsedElement frag = in.readFragment();
		assertNotNull(frag);
		assertTrue(frag instanceof PrintableFragment);
		PrintableFragment print = (PrintableFragment)frag;
		assertEquals("Hä", print.getText());

		frag = in.readFragment();
		assertNotNull(frag);
		assertEquals(AParsedElement.Type.C1, frag.type);
		assertTrue(frag instanceof C1Fragment);
		assertEquals(C1Code.CSI, ((C1Fragment)frag).getCode());
		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void test3() throws IOException {
		byte[] buf = new byte[] {0x1b, 0x5f, 0x47, 0x69, 0x3d, 0x33, 0x31, 0x3b, 0x4f, 0x4b, 0x1b, 0x5c, 0x1b,
				0x50, 0x30, 0x2b, 0x72, 0x36, 0x33, 0x33, 0x35, 0x33, 0x34, 0x33, 0x34, 0x36, 0x35, 0x1b, 0x5c};

		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ANSIInputStream in = new ANSIInputStream(bais);
		in.setEncoding(StandardCharsets.UTF_8);
		AParsedElement frag = in.readFragment();
		assertNotNull(frag);
		assertTrue(frag instanceof StringMessageFragment);
		StringMessageFragment print = (StringMessageFragment)frag;
		assertEquals("Gi=31;OK", print.getData());

		frag = in.readFragment();
		assertNotNull(frag);
		assertEquals(AParsedElement.Type.COMMAND, frag.type);
		assertTrue(frag instanceof DeviceControlFragment);
		assertEquals(C1Code.DCS, ((DeviceControlFragment)frag).getCode());
		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void test4() throws IOException {
		byte[] buf = new byte[] {27, 91, 48, 33, 27, 55};
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);

		ANSIInputStream in = new ANSIInputStream(bais);
		AParsedElement frag = in.readFragment();
		assertNotNull(frag);
		assertTrue("Expected CSI but got "+frag.getClass(),frag instanceof ControlSequenceFragment);
		ControlSequenceFragment print = (ControlSequenceFragment)frag;
		assertEquals("QRIP", print.getName());
		in.close();
	}
}
