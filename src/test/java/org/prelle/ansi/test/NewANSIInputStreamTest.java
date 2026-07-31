package org.prelle.ansi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.prelle.ansi.ANSIInputStreamFilter;
import org.prelle.ansi.AParsedElement;
import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.NewANSIInputStream;
import org.prelle.ansi.PrintableFragment;
import org.prelle.ansi.commands.AllCommands;
import org.prelle.ansi.commands.SelectGraphicRendition;

public class NewANSIInputStreamTest {

	//-------------------------------------------------------------------
	@Test
	public void testPureASCIISingle() throws IOException {
		String data = "Hello World\r\nHow are you?";
		byte[] raw = data.getBytes(StandardCharsets.US_ASCII);

		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		NewANSIInputStream in = new NewANSIInputStream(bais);

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
		NewANSIInputStream in = new NewANSIInputStream(bais);

		for (int i = 0; i < raw.length; i++) {
			int c = in.read();
			assertEquals(raw[i] & 0xFF, c);
		}
		assertEquals(-1, in.read());
		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testReadFragment() throws IOException {
		String data = "H\u001b[0mB";
		byte[] raw = data.getBytes(StandardCharsets.UTF_8);

		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		NewANSIInputStream in = new NewANSIInputStream(bais);

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
		NewANSIInputStream in = new NewANSIInputStream(bais);
		in.setCollectPrintable(true);

		byte[] buf = new byte[64];
		// 1. First read should return printable text "Hello World!" (12 bytes) before non-printable sequence \u001b[0m
		int num1 = in.read(buf, 0, 64);
		assertEquals(12, num1);
		assertEquals("Hello World!", new String(buf, 0, num1, StandardCharsets.UTF_8));

		// 2. Second read should return non-printable sequence \u001b[0m (4 bytes)
		int num2 = in.read(buf, 0, 64);
		assertEquals(4, num2);
		assertEquals("\u001b[0m", new String(buf, 0, num2, StandardCharsets.UTF_8));

		// 3. Third read should return "After" (5 bytes)
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
		NewANSIInputStream in = new NewANSIInputStream(bais);
		in.setCollectPrintable(false);

		byte[] buf = new byte[64];
		// With collectPrintable = false, read(byte[]) returns after every single fragment (character)
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
		NewANSIInputStream in = new NewANSIInputStream(bais);
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
				return List.of(); // Consume element
			}
		});

		byte[] buf = new byte[64];
		// Because \u001b[0m is consumed by filter, all printables are collected together: "Hello World!" (12 bytes)
		int num = in.read(buf, 0, 64);
		assertEquals(12, num);
		assertEquals("Hello World!", new String(buf, 0, num, StandardCharsets.UTF_8));
		assertEquals(1, filteredOut.size());

		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testUTF8MultiByteBoundaryNotSplit() throws IOException {
		// "ABC" (3 bytes), \u001b[0m (4 bytes), "ä" (2 bytes), "DEF" (3 bytes), \u001b[0m (4 bytes), "╔" (3 bytes)
		String data = "ABC\u001b[0mäDEF\u001b[0m╔";
		byte[] raw = data.getBytes(StandardCharsets.UTF_8);

		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		NewANSIInputStream in = new NewANSIInputStream(bais);
		in.setCollectPrintable(true);

		byte[] buf = new byte[4];

		// Read 1: "ABC" (3 bytes)
		int num1 = in.read(buf, 0, 4);
		assertEquals(3, num1);
		assertEquals("ABC", new String(buf, 0, num1, StandardCharsets.UTF_8));

		// Read 2: \u001b[0m (4 bytes)
		int num2 = in.read(buf, 0, 4);
		assertEquals(4, num2);
		assertEquals("\u001b[0m", new String(buf, 0, num2, StandardCharsets.UTF_8));

		// Read 3: "äDE" (2 + 2 = 4 bytes)
		int num3 = in.read(buf, 0, 4);
		assertEquals(4, num3);
		assertEquals("äDE", new String(buf, 0, num3, StandardCharsets.UTF_8));

		// Read 4: "F" (1 byte)
		int num4 = in.read(buf, 0, 4);
		assertEquals(1, num4);
		assertEquals("F", new String(buf, 0, num4, StandardCharsets.UTF_8));

		// Read 5: \u001b[0m (4 bytes)
		int num5 = in.read(buf, 0, 4);
		assertEquals(4, num5);
		assertEquals("\u001b[0m", new String(buf, 0, num5, StandardCharsets.UTF_8));

		// Read 6: "╔" (3 bytes)
		int num6 = in.read(buf, 0, 4);
		assertEquals(3, num6);
		assertEquals("╔", new String(buf, 0, num6, StandardCharsets.UTF_8));

		assertEquals(-1, in.read(buf, 0, 4));
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
		NewANSIInputStream in = new NewANSIInputStream(bais);
		in.addFilter(new XmlTagFilter());

		// 1. "Next word is " (PrintableFragment)
		AParsedElement f1 = in.readFragment();
		assertNotNull(f1);
		assertTrue(f1 instanceof PrintableFragment);
		assertEquals("Next word is ", ((PrintableFragment) f1).getText());

		// 2. ANSI Red sequence (\u001b[31m)
		AParsedElement f2 = in.readFragment();
		assertNotNull(f2);
		assertTrue(f2 instanceof ControlSequenceFragment);
		assertEquals("\u001b[31m", new String(f2.getRaw(), StandardCharsets.UTF_8));

		// 3. "red" (PrintableFragment)
		AParsedElement f3 = in.readFragment();
		assertNotNull(f3);
		assertTrue(f3 instanceof PrintableFragment);
		assertEquals("red", ((PrintableFragment) f3).getText());

		// 4. ANSI Reset sequence (\u001b[0m)
		AParsedElement f4 = in.readFragment();
		assertNotNull(f4);
		assertTrue(f4 instanceof ControlSequenceFragment);
		assertEquals("\u001b[0m", new String(f4.getRaw(), StandardCharsets.UTF_8));

		// 5. " or " (PrintableFragment)
		AParsedElement f5 = in.readFragment();
		assertNotNull(f5);
		assertTrue(f5 instanceof PrintableFragment);
		assertEquals(" or ", ((PrintableFragment) f5).getText());

		// 6. ANSI Green sequence (\u001b[32m)
		AParsedElement f6 = in.readFragment();
		assertNotNull(f6);
		assertTrue(f6 instanceof ControlSequenceFragment);
		assertEquals("\u001b[32m", new String(f6.getRaw(), StandardCharsets.UTF_8));

		// 7. "green" (PrintableFragment)
		AParsedElement f7 = in.readFragment();
		assertNotNull(f7);
		assertTrue(f7 instanceof PrintableFragment);
		assertEquals("green", ((PrintableFragment) f7).getText());

		// 8. ANSI Reset sequence (\u001b[0m)
		AParsedElement f8 = in.readFragment();
		assertNotNull(f8);
		assertTrue(f8 instanceof ControlSequenceFragment);
		assertEquals("\u001b[0m", new String(f8.getRaw(), StandardCharsets.UTF_8));

		// 9. EOF
		assertEquals(null, in.readFragment());
		in.close();
	}
}
