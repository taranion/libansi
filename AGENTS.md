# AGENTS.md

This document serves as context, architectural guidance, and operational knowledge for AI coding agents working on `libansi`.

---

## 1. Project Overview

`libansi` is a Java library designed for parsing, processing, filtering, and streaming ANSI/VT500 escape sequences and UTF-8 text from raw `InputStream` sources.

* **Package**: `org.prelle.ansi`
* **Target Java Version**: Java 21+
* **Build System**: Maven (`pom.xml`)

---

## 2. Core Architecture & Design Patterns

### `NewANSIInputStream` (`org.prelle.ansi.NewANSIInputStream`)
`NewANSIInputStream` is the primary, modern input stream class. It extends `java.io.InputStream` and implements `FilteringANSIStream`.

* **VT500 Parser**: Uses `VT500Parser` internally to parse bytes from the underlying `InputStream` into structured `AParsedElement` fragments.
* **Printable Fragment Collection (`collectPrintable`)**:
  * Controlled via `setCollectPrintable(boolean)`.
  * When `collectPrintable == true` (default), consecutive printable characters are collected into a single `PrintableFragment`.
  * The stream automatically flushes the collected printable buffer into a single `PrintableFragment` and passes it through the filter pipeline whenever a non-printable ANSI sequence or stream EOF is reached.
  * This design keeps `ANSIInputStreamFilter` implementations completely **stateless** and free of manual buffer/flush management.
* **Filter Pipeline**:
  * Filters implement `ANSIInputStreamFilter` (`handles(AParsedElement)` and `process(AParsedElement)`).
  * Filter processing occurs when fragments are enqueued before being placed into the stream's internal queue.
  * Filters can consume fragments (return empty list), pass them through intact, or expand/replace a fragment into multiple fragments.

---

## 3. Stream Operational Rules & Contracts

1. **`readFragment()`**:
   * Blocking read that returns the next parsed and filtered `AParsedElement` fragment.
   * Returns `null` on EOF (`-1`).

2. **`read()`**:
   * Blocking byte-wise read (`int`).
   * Bytes from multi-byte UTF-8 fragments are streamed sequentially across calls.

3. **`read(byte[] b, int off, int len)`**:
   * Blocking block read into a byte buffer.
   * Returns after a single fragment unless `collectPrintable` is `true` for `PrintableFragment`s and further data is immediately available (`in.available() > 0` or queued).
   * Does not split a multi-byte fragment across buffer boundaries if `i > 0` bytes have already been written into the buffer.

---

## 4. Key Symbol Hierarchy

* **`FilteringANSIStream`**: Interface providing `addFilter(ANSIInputStreamFilter filter)`.
* **`ANSIInputStreamFilter`**:
  * `boolean handles(AParsedElement event)`
  * `List<AParsedElement> process(AParsedElement event)`
* **`AParsedElement`**: Abstract base class for parsed fragments.
  * `PrintableFragment`: Contains text and code points (`getText()`, `getData()`).
  * `ControlSequenceFragment`: CSI sequences (e.g. SGR colors, cursor movements).
  * `C0Fragment`, `C1Fragment`: C0/C1 control codes.
  * `EscapeSequenceFragment`: ESC sequences.
  * `DeviceControlFragment`: DCS strings.
  * `StringMessageFragment`: OSC / SOS / PM / APC messages.
  * `KeyCodeFragment`: Special key sequences.
* **`AllCommands`**: Factory/utility class for parsing control sequences (`parseControlSequence`, `parseDeviceControlSequence`).

---

## 5. Build and Test Commands

* **Compile codebase**:
  ```bash
  mvn clean compile
  ```
* **Run tests**:
  ```bash
  mvn test
  ```
* **Run specific test class**:
  ```bash
  mvn test -Dtest=NewANSIInputStreamTest
  ```
