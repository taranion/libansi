package org.prelle.ansi.commands.kitty;

/**
 *
 */
public class KittyImageTransmission extends KittyGraphicsFragment {

	public final static char KEY_FORMAT='f';
	public final static char KEY_MEDIUM='t';
	public final static char KEY_WIDTH='s';
	public final static char KEY_HEIGHT='v';
	public final static char KEY_FILESIZE='S';
	public final static char KEY_OFFSET='O';
	public final static char KEY_ID    ='i';
	public final static char KEY_NUMBER='I';
	public final static char KEY_PLACEMENT='p';
	public final static char KEY_COMPRESSION='o';
	public final static char KEY_MORE_CHUNKS='m';

	public final static int FORMAT_RGB = 24;
	public final static int FORMAT_RGBA = 32; // Default
	public final static int FORMAT_PNG  = 100;

	public final static char MEDIUM_DIRECT = 'd'; // Default
	public final static char MEDIUM_FILE   = 'f';
	public final static char MEDIUM_TEMPFILE = 't';
	public final static char MEDIUM_SHAREDMEM = 's';

	//-------------------------------------------------------------------
	public KittyImageTransmission() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	public KittyImageTransmission setFormat(int value) {
		if (value==0)
			controlData.remove(KEY_FORMAT);
		else
			super.set(KEY_FORMAT, value);
		return this;
	}

	//-------------------------------------------------------------------
	public KittyImageTransmission setWidth(int value) {
		if (value==0)
			controlData.remove(KEY_WIDTH);
		else
			super.set(KEY_WIDTH, value);
		return this;
	}

	//-------------------------------------------------------------------
	public KittyImageTransmission setHeight(int value) {
		if (value==0)
			controlData.remove(KEY_HEIGHT);
		else
			super.set(KEY_HEIGHT, value);
		return this;
	}

	//-------------------------------------------------------------------
	public KittyImageTransmission setMoreChunksFollow(boolean more) {
		super.set(KEY_MORE_CHUNKS, more?1:0);
		return this;
	}

	//-------------------------------------------------------------------
	public KittyImageTransmission setMedium(char value) {
		super.set(KEY_MEDIUM, value);
		return this;
	}

}
