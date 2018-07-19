package selim.geyser.resources.shared;

import io.netty.buffer.ByteBuf;

public class ByteBufUtilsShared {

	public static void writeByteArray(ByteBuf buf, byte[] arr) {
		buf.writeInt(arr.length);
		for (byte b : arr)
			buf.writeByte(b);
	}

	public static byte[] readByteArray(ByteBuf buf) {
		byte[] bytes = new byte[buf.readInt()];
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = buf.readByte();
		return bytes;
	}

}
