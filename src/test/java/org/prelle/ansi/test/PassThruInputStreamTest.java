package org.prelle.ansi.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.prelle.ansi.PassthroughANSIInputStream;

/**
 * 
 */
public class PassThruInputStreamTest {

	//-------------------------------------------------------------------
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	//-------------------------------------------------------------------
	/**
	 * Read an ASCII only stream byte by byte and verify that the data is returned correctly.
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPureASCIITextPassThruSingle() throws IOException {
		String data = "Hello World\r\nHow are you?";

		// Read byte by byte
		ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes(StandardCharsets.US_ASCII));
		PassthroughANSIInputStream in = new PassthroughANSIInputStream(bais);
		for (int i=0; i<data.length(); i++) {
			int c = in.read();
			assertEquals(data.charAt(i), (char)c);
		}
		// Next byte should be end of stream
		assertEquals(-1, in.read());
		in.close();
	}

	//-------------------------------------------------------------------
	/**
	 * Read an ASCII only stream in a single block and verify that the data is returned correctly.
	 */
	@Test
	public void testPureASCIITextPassThruBlock() throws IOException {
		String data = "Hello World\r\nHow are you?";

		// Read full block
		ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes(StandardCharsets.US_ASCII));
		PassthroughANSIInputStream in = new PassthroughANSIInputStream(bais);
		byte[] buf = new byte[64];
		int num = in.read(buf);
		assertEquals(data.length(), num);
		// Check result
		String readStr = new String(buf, 0, num, StandardCharsets.US_ASCII);
		assertEquals(data, readStr);
		// Next byte should be end of stream
		assertEquals(-1, in.read());
		in.close();
		
	}

	//-------------------------------------------------------------------
	@Test
	public void testUnicodeTextPassThruSingle() throws IOException {
		String data = "Was ist äöüß? oder ╔═╗║╚╝║─╤╧╟╢┼┴┬┤├ ?\r\nKein Emoji 😀!";
		byte[] raw = data.getBytes(StandardCharsets.UTF_8);

		// Read byte by byte
		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		PassthroughANSIInputStream in = new PassthroughANSIInputStream(bais);
		for (int i=0; i<data.length(); i++) {
			int c = in.read();
			assertEquals(raw[i], 0xff&c);
		}
		// Next byte should be end of stream
		assertEquals(-1, in.read());
		in.close();
	}

	//-------------------------------------------------------------------
	/**
	 * Read a UTF-8 encoded stream into a buffer that is large enough. Verify that the data is returned correctly.
	 */
	@Test
	public void testUTF8TextPassThruBlock() throws IOException {
		String data = "Was ist äöüß? oder ╔═╗║╚╝║─╤╧╟╢┼┴┬┤├ ?\r\nKein Emoji 😀!";
		byte[] raw = data.getBytes(StandardCharsets.UTF_8);

		// Read full block
		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		PassthroughANSIInputStream in = new PassthroughANSIInputStream(bais);
		byte[] buf = new byte[128];
		int num = in.read(buf);
		assertEquals(raw.length, num);
		// Check result
		String readStr = new String(buf, 0, num, StandardCharsets.UTF_8);
		assertEquals(data, readStr);
		// Next byte should be end of stream
		assertEquals(-1, in.read());
		in.close();
	}

	//-------------------------------------------------------------------
	/**
	 * Read a UTF-8 encoded stream into a buffer that is not large enough
	 * and read must be split into multiple calls.
	 */
	@Test
	public void testUTF8TextPassThruBlockSegmented() throws IOException {
		String data = "Was ist äöüß? oder ╔═╗║╚╝║─╤╧╟╢┼┴┬┤├ ?\r\nKein Emoji 😀!";
		byte[] raw = data.getBytes(StandardCharsets.UTF_8);

		// Read full block
		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		PassthroughANSIInputStream in = new PassthroughANSIInputStream(bais);
		byte[] buf = new byte[32];
		int[] lengths = new int[] {32, 32, raw.length-64};
		for (int i=0; i<3; i++) {
			int num = in.read(buf);
			assertEquals(lengths[i], num);
			// Check result
			byte[] expect = new byte[lengths[i]];
			System.arraycopy(raw, i*32, expect, 0, lengths[i]);
			String readStr = new String(buf, 0, num, StandardCharsets.UTF_8);
			String exptStr = new String(expect, StandardCharsets.UTF_8);
			assertEquals(exptStr, readStr);
		}
		// Next byte should be end of stream
		assertEquals(-1, in.read());
		in.close();
	}

	//-------------------------------------------------------------------
	// TODO: A unit test that has a 2- or 3-byte UTF-8, where the first byte would fit 
	// into the buffer, buf the remaining not anymore. Splitting should be done in a way
	// that the first byte is not returned, but the whole character is returned in the next read.

}
