package selim.geyser.resources.forge;

import java.io.File;

import net.minecraft.client.resources.FileResourcePack;

public class AssetPackResourcePack extends FileResourcePack {

	public AssetPackResourcePack(String pack) {
		this(new File(PackManager.PACK_FOLDER, pack));
	}

	public AssetPackResourcePack(File file) {
		super(file);
	}

}
