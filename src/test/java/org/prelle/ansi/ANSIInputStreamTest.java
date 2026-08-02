package org.prelle.ansi;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.prelle.ansi.commands.AllCommands;
import org.prelle.ansi.commands.SelectGraphicRendition;

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

	//-------------------------------------------------------------------
	@Test
	public void testPureASCIISingle() throws IOException {
		String data = "Hello World\r\nHow are you?";
		byte[] raw = data.getBytes(StandardCharsets.US_ASCII);

		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		ANSIInputStream in = new ANSIInputStream(bais);

		for (int i = 0; i < raw.length; i++) {
			int c = in.read();
			assertEquals(raw[i] & 0xFF, c);
		}
		assertEquals(-1, in.read());
		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testUnicodeSingle() throws IOException {
		String data = "Was ist äöüß? oder ╔═╗║╚╝║─╤╧╟╢┼┴┬┤├ ?\r\nKein Emoji 😀!";
		byte[] raw = data.getBytes(StandardCharsets.UTF_8);

		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		ANSIInputStream in = new ANSIInputStream(bais);

		for (int i = 0; i < raw.length; i++) {
			int c = in.read();
			assertEquals(raw[i] & 0xFF, c);
		}
		assertEquals(-1, in.read());
		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testUnicodeReadByteArray() throws IOException {
		String data = "Was ist äöüß? oder ╔═╗║╚╝║─╤╧╟╢┼┴┬┤├ ?\r\nKein Emoji 😀!";
		byte[] raw = data.getBytes(StandardCharsets.UTF_8);

		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		ANSIInputStream in = new ANSIInputStream(bais);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int bytesRead;
		while ((bytesRead = in.read(buf)) != -1) {
			baos.write(buf, 0, bytesRead);
		}
		byte[] result = baos.toByteArray();

		assertEquals(raw.length, result.length);
		for (int i = 0; i < raw.length; i++) {
			assertEquals(raw[i] & 0xFF, result[i] & 0xFF);
		}
		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testReadFragment() throws IOException {
		String data = "H\u001b[0mB";
		byte[] raw = data.getBytes(StandardCharsets.UTF_8);

		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		ANSIInputStream in = new ANSIInputStream(bais);

		AParsedElement frag1 = in.readFragment();
		assertNotNull(frag1);
		assertTrue(frag1 instanceof PrintableFragment);

		AParsedElement frag2 = in.readFragment();
		assertNotNull(frag2);
		assertTrue(frag2 instanceof ControlSequenceFragment);

		AParsedElement frag3 = in.readFragment();
		assertNotNull(frag3);
		assertTrue(frag3 instanceof PrintableFragment);

		assertEquals(null, in.readFragment());
		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testBlockReadCollectPrintableTrue() throws IOException {
		String data = "Hello World!\u001b[0mAfter";
		byte[] raw = data.getBytes(StandardCharsets.UTF_8);

		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		ANSIInputStream in = new ANSIInputStream(bais);
		in.setCollectPrintable(true);

		byte[] buf = new byte[64];
		int num1 = in.read(buf, 0, 64);
		assertEquals(12, num1);
		assertEquals("Hello World!", new String(buf, 0, num1, StandardCharsets.UTF_8));

		int num2 = in.read(buf, 0, 64);
		assertEquals(4, num2);
		assertEquals("\u001b[0m", new String(buf, 0, num2, StandardCharsets.UTF_8));

		int num3 = in.read(buf, 0, 64);
		assertEquals(5, num3);
		assertEquals("After", new String(buf, 0, num3, StandardCharsets.UTF_8));

		assertEquals(-1, in.read(buf, 0, 64));
		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testBlockReadCollectPrintableFalse() throws IOException {
		String data = "Hello";
		byte[] raw = data.getBytes(StandardCharsets.UTF_8);

		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		ANSIInputStream in = new ANSIInputStream(bais);
		in.setCollectPrintable(false);

		byte[] buf = new byte[64];
		int num1 = in.read(buf, 0, 64);
		assertEquals(1, num1);
		assertEquals('H', (char)buf[0]);

		int num2 = in.read(buf, 0, 64);
		assertEquals(1, num2);
		assertEquals('e', (char)buf[0]);

		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testFiltering() throws IOException {
		String data = "Hello \u001b[0mWorld!";
		byte[] raw = data.getBytes(StandardCharsets.UTF_8);

		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		ANSIInputStream in = new ANSIInputStream(bais);
		in.setCollectPrintable(true);

		List<AParsedElement> filteredOut = new ArrayList<>();
		in.addFilter(new ANSIInputStreamFilter() {
			@Override
			public boolean handles(AParsedElement event) {
				return event instanceof SelectGraphicRendition;
			}

			@Override
			public List<AParsedElement> process(AParsedElement event) {
				filteredOut.add(event);
				return List.of();
			}
		});

		byte[] buf = new byte[64];
		int num = in.read(buf, 0, 64);
		assertEquals(12, num);
		assertEquals("Hello World!", new String(buf, 0, num, StandardCharsets.UTF_8));
		assertEquals(1, filteredOut.size());

		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testUTF8MultiByteBoundaryNotSplit() throws IOException {
		String data = "ABC\u001b[0mäDEF\u001b[0m╔";
		byte[] raw = data.getBytes(StandardCharsets.UTF_8);

		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		ANSIInputStream in = new ANSIInputStream(bais);
		in.setCollectPrintable(true);

		byte[] buf = new byte[4];

		int num1 = in.read(buf, 0, 4);
		assertEquals(3, num1);
		assertEquals("ABC", new String(buf, 0, num1, StandardCharsets.UTF_8));

		int num2 = in.read(buf, 0, 4);
		assertEquals(4, num2);
		assertEquals("\u001b[0m", new String(buf, 0, num2, StandardCharsets.UTF_8));

		int num3 = in.read(buf, 0, 4);
		assertEquals(4, num3);
		assertEquals("äDE", new String(buf, 0, num3, StandardCharsets.UTF_8));

		int num4 = in.read(buf, 0, 4);
		assertEquals(1, num4);
		assertEquals("F", new String(buf, 0, num4, StandardCharsets.UTF_8));

		int num5 = in.read(buf, 0, 4);
		assertEquals(4, num5);
		assertEquals("\u001b[0m", new String(buf, 0, num5, StandardCharsets.UTF_8));

		int num6 = in.read(buf, 0, 4);
		assertEquals(3, num6);
		assertEquals("╔", new String(buf, 0, num6, StandardCharsets.UTF_8));

		assertEquals(-1, in.read(buf, 0, 4));
		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testUTF8FollowedBySGRNoByteLeak() throws IOException {
		String data = "║\u001b[48;5;10m";
		byte[] raw = data.getBytes(StandardCharsets.UTF_8);

		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		ANSIInputStream in = new ANSIInputStream(bais);

		AParsedElement frag1 = in.readFragment();
		assertNotNull(frag1);
		assertTrue(frag1 instanceof PrintableFragment);
		assertEquals("║", ((PrintableFragment) frag1).getText());
		assertEquals(3, frag1.getRaw().length);

		AParsedElement frag2 = in.readFragment();
		assertNotNull(frag2);
		assertTrue(frag2 instanceof ControlSequenceFragment);
		byte[] raw2 = frag2.getRaw();
		assertEquals(10, raw2.length);
		assertEquals("\u001b[48;5;10m", new String(raw2, StandardCharsets.UTF_8));

		in.close();
	}

	//-------------------------------------------------------------------
	private static class XmlTagFilter implements ANSIInputStreamFilter {

		@Override
		public boolean handles(AParsedElement event) {
			return event instanceof PrintableFragment;
		}

		@Override
		public List<AParsedElement> process(AParsedElement event) {
			if (!(event instanceof PrintableFragment print)) {
				return List.of(event);
			}

			String text = print.getText();
			List<AParsedElement> result = new ArrayList<>();
			int cursor = 0;

			while (cursor < text.length()) {
				int openTag = text.indexOf("<", cursor);
				if (openTag == -1) {
					String remaining = text.substring(cursor);
					result.add(new PrintableFragment(remaining).setRaw(remaining.getBytes(StandardCharsets.UTF_8)));
					break;
				}

				if (openTag > cursor) {
					String plain = text.substring(cursor, openTag);
					result.add(new PrintableFragment(plain).setRaw(plain.getBytes(StandardCharsets.UTF_8)));
				}

				int closeTag = text.indexOf(">", openTag);
				if (closeTag == -1) {
					String remaining = text.substring(openTag);
					result.add(new PrintableFragment(remaining).setRaw(remaining.getBytes(StandardCharsets.UTF_8)));
					break;
				}

				String tag = text.substring(openTag, closeTag + 1);
				cursor = closeTag + 1;

				AParsedElement ansiSeq = mapTagToAnsi(tag);
				if (ansiSeq != null) {
					result.add(ansiSeq);
				}
			}

			return result;
		}

		private AParsedElement mapTagToAnsi(String tag) {
			return switch (tag) {
				case "<red>" -> AllCommands.parseControlSequence(109, "", "31").setRaw("\u001b[31m".getBytes(StandardCharsets.UTF_8));
				case "</red>", "</g>", "</green>" -> AllCommands.parseControlSequence(109, "", "0").setRaw("\u001b[0m".getBytes(StandardCharsets.UTF_8));
				case "<g>", "<green>" -> AllCommands.parseControlSequence(109, "", "32").setRaw("\u001b[32m".getBytes(StandardCharsets.UTF_8));
				default -> new PrintableFragment(tag).setRaw(tag.getBytes(StandardCharsets.UTF_8));
			};
		}
	}

	//-------------------------------------------------------------------
	@Test
	public void testXmlTagToAnsiColorFilterFragments() throws IOException {
		String input = "Next word is <red>red</red> or <g>green</g>";
		byte[] raw = input.getBytes(StandardCharsets.UTF_8);

		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		ANSIInputStream in = new ANSIInputStream(bais);
		in.addFilter(new XmlTagFilter());

		AParsedElement f1 = in.readFragment();
		assertNotNull(f1);
		assertTrue(f1 instanceof PrintableFragment);
		assertEquals("Next word is ", ((PrintableFragment) f1).getText());

		AParsedElement f2 = in.readFragment();
		assertNotNull(f2);
		assertTrue(f2 instanceof ControlSequenceFragment);
		assertEquals("\u001b[31m", new String(f2.getRaw(), StandardCharsets.UTF_8));

		AParsedElement f3 = in.readFragment();
		assertNotNull(f3);
		assertTrue(f3 instanceof PrintableFragment);
		assertEquals("red", ((PrintableFragment) f3).getText());

		AParsedElement f4 = in.readFragment();
		assertNotNull(f4);
		assertTrue(f4 instanceof ControlSequenceFragment);
		assertEquals("\u001b[0m", new String(f4.getRaw(), StandardCharsets.UTF_8));

		AParsedElement f5 = in.readFragment();
		assertNotNull(f5);
		assertTrue(f5 instanceof PrintableFragment);
		assertEquals(" or ", ((PrintableFragment) f5).getText());

		AParsedElement f6 = in.readFragment();
		assertNotNull(f6);
		assertTrue(f6 instanceof ControlSequenceFragment);
		assertEquals("\u001b[32m", new String(f6.getRaw(), StandardCharsets.UTF_8));

		AParsedElement f7 = in.readFragment();
		assertNotNull(f7);
		assertTrue(f7 instanceof PrintableFragment);
		assertEquals("green", ((PrintableFragment) f7).getText());

		AParsedElement f8 = in.readFragment();
		assertNotNull(f8);
		assertTrue(f8 instanceof ControlSequenceFragment);
		assertEquals("\u001b[0m", new String(f8.getRaw(), StandardCharsets.UTF_8));

		assertEquals(null, in.readFragment());
		in.close();
	}
}
