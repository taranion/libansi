package org.prelle.ansi.commands.iterm;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * https://iterm2.com/documentation-images.html
 */
public class SendITermImage extends ITermCommandFragment {

	private String name;
	private Integer size;
	private Integer width;
	private Integer height;
	private Boolean preserveAspectRation;
	private Boolean inline;
	private byte[] imgData;

	//-------------------------------------------------------------------
	public SendITermImage() {
		super(null);
	}

	//-------------------------------------------------------------------
	public SendITermImage(String data) {
		super(data);
	}

	//-------------------------------------------------------------------
	public String getFileName() {
		return name;
	}

	//-------------------------------------------------------------------
	/**
	 * @param name the name to set
	 */
	public void setFileName(String name) {
		this.name = name;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the size
	 */
	public Integer getSize() {
		return size;
	}

	//-------------------------------------------------------------------
	/**
	 * @param size the size to set
	 */
	public void setSize(Integer size) {
		this.size = size;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the width
	 */
	public Integer getWidth() {
		return width;
	}

	//-------------------------------------------------------------------
	/**
	 * @param width the width to set
	 */
	public void setWidth(Integer width) {
		this.width = width;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the height
	 */
	public Integer getHeight() {
		return height;
	}

	//-------------------------------------------------------------------
	/**
	 * @param height the height to set
	 */
	public void setHeight(Integer height) {
		this.height = height;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the preserveAspectRation
	 */
	public Boolean getPreserveAspectRation() {
		return preserveAspectRation;
	}

	//-------------------------------------------------------------------
	/**
	 * @param preserveAspectRation the preserveAspectRation to set
	 */
	public void setPreserveAspectRation(Boolean preserveAspectRation) {
		this.preserveAspectRation = preserveAspectRation;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the inline
	 */
	public Boolean getInline() {
		return inline;
	}

	//-------------------------------------------------------------------
	/**
	 * @param inline the inline to set
	 */
	public void setInline(Boolean inline) {
		this.inline = inline;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the imgData
	 */
	public byte[] getImgData() {
		return imgData;
	}

	//-------------------------------------------------------------------
	/**
	 * @param imgData the imgData to set
	 */
	public void setImgData(byte[] imgData) {
		this.imgData = imgData;
	}

	//-------------------------------------------------------------------
	private void buildCommand() {
		StringBuffer buf = new StringBuffer("1337;File=");

		List<String> pairs = new ArrayList<>();
		if (inline==null || inline) {
			pairs.add("inline=1");
		}
		pairs.add("size="+imgData.length);
		if (width!=null) {
			if (width>80)
				pairs.add("width="+width+"px");
			else
				pairs.add("width="+width);
		}
		if (height!=null) {
			if (height>40)
				pairs.add("height="+height+"px");
			else
				pairs.add("height="+height);
		}
		if (preserveAspectRation!=null) {
			pairs.add("preserveAspectRation="+(preserveAspectRation?1:0));
		}
		buf.append(String.join(";", pairs));
		if (imgData!=null) {
			buf.append(":");
			buf.append(Base64.getEncoder().encodeToString(imgData));
		}

		data = buf.toString();
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.SequenceFragment#encode(java.io.ByteArrayOutputStream, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		buildCommand();
		super.encode(toFill, use7Bit);
	}

}
