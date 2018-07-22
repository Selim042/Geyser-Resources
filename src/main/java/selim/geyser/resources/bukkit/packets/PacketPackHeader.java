package selim.geyser.resources.bukkit.packets;

import io.netty.buffer.ByteBuf;
import selim.geyser.core.bukkit.BukkitByteBufUtils;
import selim.geyser.core.bukkit.network.GeyserPacket;
import selim.geyser.resources.shared.ByteBufUtilsShared;

public class PacketPackHeader extends GeyserPacket {

	private String name;
	private int numPackets;
	private byte[] md5;

	public PacketPackHeader() {}

	public PacketPackHeader(String name, int numPackets) {
		this(name, numPackets, new byte[0]);
	}

	public PacketPackHeader(String name, int numPackets, byte[] md5) {
		this.name = name;
		this.numPackets = numPackets;
		this.md5 = md5;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.name = BukkitByteBufUtils.readUTF8String(buf);
		this.numPackets = buf.readInt();
		this.md5 = ByteBufUtilsShared.readByteArray(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		BukkitByteBufUtils.writeUTF8String(buf, this.name);
		buf.writeInt(this.numPackets);
		ByteBufUtilsShared.writeByteArray(buf, this.md5);
	}

}
