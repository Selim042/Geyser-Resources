package selim.geyser.resources.bukkit.packets;

import io.netty.buffer.ByteBuf;
import selim.geyser.core.bukkit.network.GeyserPacket;
import selim.geyser.core.shared.SharedByteBufUtils;

public class PacketPackData extends GeyserPacket {

	private byte[] data;

	public PacketPackData() {}

	public PacketPackData(byte[] bytes) {
		this.data = bytes;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.data = SharedByteBufUtils.readByteArray(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		SharedByteBufUtils.writeByteArray(buf, this.data);
	}

}
