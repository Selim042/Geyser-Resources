package selim.geyser.resources.bukkit.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import selim.geyser.core.bukkit.network.GeyserPacket;
import selim.geyser.resources.shared.ByteBufUtilsShared;

public class PacketPackData extends GeyserPacket {

	private String name;
	private byte[] data;

	@Override
	public void fromBytes(ByteBuf buf) {
		this.name = ByteBufUtils.readUTF8String(buf);
		this.data = ByteBufUtilsShared.readByteArray(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.name);
		ByteBufUtilsShared.writeByteArray(buf, this.data);
	}

}
