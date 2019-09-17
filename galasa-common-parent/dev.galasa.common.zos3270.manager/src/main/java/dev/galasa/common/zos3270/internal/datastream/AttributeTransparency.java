package dev.galasa.common.zos3270.internal.datastream;

import java.nio.ByteBuffer;

public class AttributeTransparency implements IAttribute {
	
	public final static byte ATTRIBUTE_ID = 0x46;
	
	private final byte transparency;
	
	public AttributeTransparency(ByteBuffer buffer) {
		this.transparency = buffer.get();
	}

}