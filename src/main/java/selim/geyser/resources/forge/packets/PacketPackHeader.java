package selim.geyser.resources.forge.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import selim.geyser.core.shared.SharedByteBufUtils;
import selim.geyser.resources.forge.PackManager;

public class PacketPackHeader implements IMessage {

	private String name;
	private int numPackets;
	private byte[] md5;

	@Override
	public void fromBytes(ByteBuf buf) {
		this.name = ByteBufUtils.readUTF8String(buf);
		this.numPackets = buf.readInt();
		this.md5 = SharedByteBufUtils.readByteArray(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.name);
		buf.writeInt(this.numPackets);
		SharedByteBufUtils.writeByteArray(buf, this.md5);
	}

	public static class Handler implements IMessageHandler<PacketPackHeader, IMessage> {

		@Override
		public IMessage onMessage(PacketPackHeader message, MessageContext ctx) {
			PackManager.startDataList(message.name, message.numPackets, message.md5);
			return null;
		}

	}

}
