package selim.geyser.resources.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class GuiDownloadingPacks extends GuiScreen {

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		String text = "Downloading " + PackManager.getDownloadingPack() + " ("
				+ PackManager.getDownloadingProgress() + "%)\n" + PackManager.getNumPacks()
				+ " packs remaining";
		int textWidth = this.fontRenderer.getStringWidth(text);
		drawRect((this.width / 2) - (textWidth / 2) + 2, (this.height / 2) - 10,
				(this.width / 2) + (textWidth / 2) + 2, (this.height / 2) + 10, 0xFF777777);
		this.fontRenderer.drawString(text, (this.width / 2) - (textWidth / 2), (this.height / 2) - 8,
				0xFFFFFF);
		if (PackManager.getNumPacks() == 0)
			Minecraft.getMinecraft().currentScreen = null;
	}

	@Override
	public void onGuiClosed() {
		Minecraft.getMinecraft().currentScreen = this;
	}

}
