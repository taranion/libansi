package org.prelle.ansi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.net.SocketTimeoutException;
import java.util.Objects;

/**
 * 
 */
public class PassthroughANSIInputStream extends AbstractANSIInputStream {

	private byte[] blockFragment;
	private int blockOffset;
	private long lastReleaseTime;
	private Thread autoReleaseThread;

	//-------------------------------------------------------------------
	/**
	 * @param in
	 */
	public PassthroughANSIInputStream(InputStream in) {
		super(in);
		collectPrintable = false;
		
		if (collectPrintable) {
			autoReleaseThread = new Thread(() -> {
				while (true) {
					try {
						Thread.sleep(100);
						if (System.currentTimeMillis() - lastReleaseTime > 100) 
							releaseBuffer();
					} catch (InterruptedException e) {
						break;
					}
				}
			});
			autoReleaseThread.setDaemon(true);
			autoReleaseThread.start();
		}
	}

	//-------------------------------------------------------------------
	private int ensureBlockFragment() throws IOException {
		if (blockFragment == null || blockOffset == blockFragment.length) {
			AParsedElement fragment = readFragment();
			if (fragment == null) return -1;
//			logger.log(Level.ERROR, "fragment: {0}={1}", fragment, fragment.rawData);
			blockFragment = fragment.getRaw();
			blockOffset = 0;
		}
		return blockFragment.length - blockOffset;
	}

	//-------------------------------------------------------------------
	public int available() throws IOException {
		if (blockFragment == null || blockOffset == blockFragment.length) {
			return in.available();
		}
		return blockFragment.length - blockOffset;
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
			
        	lastReleaseTime = System.currentTimeMillis();
        	if (blockFragment == null || blockOffset == blockFragment.length) {
				return -1;
			}
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
        	lastReleaseTime = System.currentTimeMillis();
            return 0;
        }

        int i=0;
        try {
			while (i < len) {
				int c = read();
				if (c == -1 || filtered) {
					break;
				}
				b[off + i] = (byte)c;
				i++;
			}
		} catch (SocketTimeoutException e) {
		}
    	lastReleaseTime = System.currentTimeMillis();
    	return i;
    }

}
