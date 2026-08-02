# LibANSI

`libansi` is a lightweight Java library for parsing, streaming, and filtering ANSI/VT500 escape sequences and UTF-8 text from input streams.

---

## Features

- **VT500 ANSI Parsing**: Full support for VT500 terminal control sequences (C0, C1, CSI, ESC, OSC, DCS).
- **Printable Fragment Grouping**: Configurable collection of printable UTF-8 text into clean `PrintableFragment` objects.
- **Stream Filtering**: Easily intercept, modify, consume, or inject ANSI sequences or text using simple, stateless filters (`ANSIInputStreamFilter`).
- **Flexible Reading Modes**:
  - **Fragment-level**: Read structured `AParsedElement` objects (`readFragment()`).
  - **Block buffer**: Read raw bytes into a buffer (`read(byte[], int, int)`).
  - **Byte-by-byte**: Stream bytes individually (`read()`).

---

## Requirements

- **Java**: 21 or higher
- **Build Tool**: Maven

---

## Getting Started

### 1. Basic Usage

Wrap any standard `InputStream` with `ANSIInputStream` to read parsed ANSI fragments or UTF-8 text:

```java
InputStream source = ...; // e.g. ByteArrayInputStream or Socket InputStream
ANSIInputStream ansiIn = new ANSIInputStream(source);

// Read fragments structured by type
AParsedElement fragment;
while ((fragment = ansiIn.readFragment()) != null) {
    if (fragment instanceof PrintableFragment print) {
        System.out.print(print.getText());
    } else if (fragment instanceof ControlSequenceFragment csi) {
        System.out.println("Encountered control sequence: " + csi);
    }
}

ansiIn.close();
```

---

### 2. Stream Filtering Example (XML Tags to ANSI Colors)

You can add custom filters to transform text or ANSI sequences on the fly. For example, replacing `<red>` tags with ANSI color codes:

```java
ANSIInputStream ansiIn = new ANSIInputStream(source);

ansiIn.addFilter(new ANSIInputStreamFilter() {
    @Override
    public boolean handles(AParsedElement event) {
        return event instanceof PrintableFragment;
    }

    @Override
    public List<AParsedElement> process(AParsedElement event) {
        PrintableFragment print = (PrintableFragment) event;
        String text = print.getText();
        List<AParsedElement> result = new ArrayList<>();

        // Replace <red> with ANSI Red sequence (\u001b[31m) and </red> with Reset (\u001b[0m)
        // ... transform text into PrintableFragment and ControlSequenceFragment objects
        
        return result;
    }
});
```

---

## Building and Testing

Build the library with Maven:

```bash
mvn clean compile
```

Run unit tests:

```bash
mvn test
```

---

## License

Distributed under the project license. See project repository details for licensing terms.