package org.prelle.ansi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.util.Objects;

/**
 * 
 */
public class PassthroughANSIInputStream extends AbstractANSIInputStream {

	private byte[] blockFragment;
	private int blockOffset;

	//-------------------------------------------------------------------
	/**
	 * @param in
	 */
	public PassthroughANSIInputStream(InputStream in) {
		super(in);
		collectPrintable = false;
	}

	//-------------------------------------------------------------------
	private int ensureBlockFragment() throws IOException {
		if (blockFragment == null || blockOffset == blockFragment.length) {
			AParsedElement fragment = readFragment();
			if (fragment == null) return -1;
			logger.log(Level.TRACE, "fragment: {0}={1}", fragment, fragment.rawData);
			blockFragment = fragment.getRaw();
			blockOffset = 0;
		}
		return blockFragment.length - blockOffset;
	}

	//-------------------------------------------------------------------
	public int available() throws IOException {
		return ensureBlockFragment();
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		logger.log(Level.DEBUG, "ENTER read()");
		filtered = false;
		try {
			ensureBlockFragment();
			
			return blockFragment[blockOffset++] & 0xFF;
		} finally {
			logger.log(Level.DEBUG, "LEAVE read()");
		}
	}
	
    //-------------------------------------------------------------------
    /**
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);
        if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte)c;

        int i = 1;
        try {
            for (; i < len ; i++) {
                c = read();
                if (c == -1 || filtered) {
                    break;
                }
                b[off + i] = (byte)c;
            }
        } catch (IOException ee) {
        }
        return i;
    }

}
