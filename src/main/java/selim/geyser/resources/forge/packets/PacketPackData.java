package selim.geyser.resources.forge.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import selim.geyser.resources.forge.PackManager;
import selim.geyser.resources.shared.ByteBufUtilsShared;

public class PacketPackData implements IMessage {

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

	public static class Handler implements IMessageHandler<PacketPackData, IMessage> {

		@Override
		public IMessage onMessage(PacketPackData message, MessageContext ctx) {
			PackManager.addDataPacket(message.name, message.data);
			return null;
		}

	}

}
