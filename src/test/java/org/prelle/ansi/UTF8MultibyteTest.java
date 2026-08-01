package org.prelle.ansi;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class UTF8MultibyteTest {

	@Test
	public void testUTF8MultibyteBoxDrawing() throws IOException {
		// Box drawing horizontal line '─' U+2500: UTF-8 0xE2 0x94 0x80
		byte[] utf8Bytes = "┌─┐".getBytes(StandardCharsets.UTF_8);

		ByteArrayInputStream bais = new ByteArrayInputStream(utf8Bytes);
		NewANSIInputStream ansiIn = new NewANSIInputStream(bais);

		byte[] outBuf = new byte[100];
		int bytesRead = ansiIn.read(outBuf, 0, outBuf.length);

		System.out.println("Input string length in bytes: " + utf8Bytes.length);
		System.out.println("NewANSIInputStream output bytes read: " + bytesRead);
		String result = new String(outBuf, 0, bytesRead, StandardCharsets.UTF_8);
		System.out.println("Decoded result string: '" + result + "'");

		assertEquals(utf8Bytes.length, bytesRead);
		assertEquals("┌─┐", result);
	}
}
