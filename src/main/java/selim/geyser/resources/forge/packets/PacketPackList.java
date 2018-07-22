package selim.geyser.resources.forge.packets;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import selim.geyser.resources.forge.PackManager;

public class PacketPackList implements IMessage {

	private List<String> zips;

	public PacketPackList() {
		this.zips = new ArrayList<>();
	}

	public PacketPackList(List<String> files) {
		this.zips = new ArrayList<String>(files);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.zips.size());
		for (String zip : zips)
			ByteBufUtils.writeUTF8String(buf, zip);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int size = buf.readInt();
		for (int i = 0; i < size; i++)
			zips.add(ByteBufUtils.readUTF8String(buf));
	}

	public static class Handler implements IMessageHandler<PacketPackList, PacketPackList> {

		@Override
		public PacketPackList onMessage(PacketPackList message, MessageContext ctx) {
			List<String> reply = new ArrayList<>();
			for (String pack : message.zips)
				if (!PackManager.hasPack(pack))
					reply.add(pack);
			PackManager.setNumPacks(reply.size());
			return new PacketPackList(reply);
		}

	}

}
