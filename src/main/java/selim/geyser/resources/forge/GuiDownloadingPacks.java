package selim.geyser.resources.forge;

import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import selim.geyser.resources.shared.GeyserResourcesInfo;

public class GuiDownloadingPacks extends GuiScreen {

	private static final ResourceLocation GUI_BACKGROUND = new ResourceLocation(GeyserResourcesInfo.ID,
			"textures/gui/downloading_packs.png");
	private static final int BACKGROUND_HEIGHT = 72;
	private static final int BACKGROUND_WIDTH = 176;
	private static final int BAR_HEIGHT = 12;
	private static final int BAR_WIDTH = 146;

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		// drawDemoScreen();
		Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_BACKGROUND);
		int midWidth = this.width / 2;
		int midHeight = this.height / 2;
		int percent = PackManager.getDownloadingProgress();
		drawModalRectWithCustomSizedTexture(midWidth - (BACKGROUND_WIDTH / 2),
				midHeight - (BACKGROUND_HEIGHT / 2), 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 256,
				256);
		int xPos = (int) (BAR_WIDTH - (BAR_WIDTH * (percent / 100.0f)));
		drawModalRectWithCustomSizedTexture(midWidth - 73, midHeight - 1, xPos, BACKGROUND_HEIGHT,
				BAR_WIDTH - xPos, BAR_HEIGHT, 256, 256);
		String text = I18n.format(GeyserResourcesInfo.ID + ":downloading",
				PackManager.getDownloadingPack());
		List<String> lines = this.fontRenderer.listFormattedStringToWidth(text, 150);
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			int lineWidth = this.fontRenderer.getStringWidth(line);
			this.fontRenderer.drawString(line, midWidth - (lineWidth / 2),
					midHeight - (-i * 10) - 6 - (lines.size() * 10), 0x000000);
		}
		String percentText = percent + "%";
		int percentPos = midWidth - 70;
		if (percent > 17)
			percentPos = midWidth - xPos + 50;
		this.fontRenderer.drawString(percentText, percentPos, midHeight + 1, 0xFFFFFF);
		String remainText = I18n.format(
				GeyserResourcesInfo.ID + ":remaining"
						+ (PackManager.getNumPacks() == 1 ? "_singular" : ""),
				PackManager.getNumPacks());
		this.fontRenderer.drawString(remainText,
				midWidth - (this.fontRenderer.getStringWidth(remainText) / 2), midHeight + 16, 0x000000);
		if (PackManager.getNumPacks() <= 0)
			Minecraft.getMinecraft().displayGuiScreen(null);
	}

	private static Random rand = new Random();
	private static int stage;
	private static int timer;
	private static int maxTime = 1000 + rand.nextInt(1000);
	private static String[] stages = new String[] { "Wands-3.1.0.zip", "Penguins-1.2.0.zip",
			"SelimsBackpacks-2.0.5.zip", "AtlasBlocks-1.2.3.zip" };

	private void drawDemoScreen() {
		if (stage == -1) {
			Minecraft.getMinecraft().currentScreen = null;
			stage = 0;
			return;
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_BACKGROUND);
		int midWidth = this.width / 2;
		int midHeight = this.height / 2;
		int percent = (100 * timer) / maxTime;
		drawModalRectWithCustomSizedTexture(midWidth - (BACKGROUND_WIDTH / 2),
				midHeight - (BACKGROUND_HEIGHT / 2), 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 256,
				256);
		int xPos = (int) (BAR_WIDTH - (BAR_WIDTH * (percent / 100.0f)));
		drawModalRectWithCustomSizedTexture(midWidth - 73, midHeight - 1, xPos, BACKGROUND_HEIGHT,
				BAR_WIDTH - xPos, BAR_HEIGHT, 256, 256);
		String text = I18n.format(GeyserResourcesInfo.ID + ":downloading", stages[stage]);
		List<String> lines = this.fontRenderer.listFormattedStringToWidth(text, 150);
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			int lineWidth = this.fontRenderer.getStringWidth(line);
			this.fontRenderer.drawString(line, midWidth - (lineWidth / 2),
					midHeight - (-i * 10) - 6 - (lines.size() * 10), 0x000000);
		}
		String percentText = percent + "%";
		int percentPos = midWidth - 70;
		if (percent > 17)
			percentPos = midWidth - xPos + 50;
		this.fontRenderer.drawString(percentText, percentPos, midHeight + 1, 0xFFFFFF);
		String remainText = I18n.format(GeyserResourcesInfo.ID + ":remaining", (stages.length - stage));
		this.fontRenderer.drawString(remainText,
				midWidth - (this.fontRenderer.getStringWidth(remainText) / 2), midHeight + 16, 0x000000);
		timer += rand.nextInt(25);
		if (timer >= maxTime) {
			maxTime = 1000 + rand.nextInt(5000);
			stage++;
			timer = 0;
			if (stage + 1 > stages.length)
				stage = 0;
		}
	}

	@Override
	public void onGuiClosed() {
		Minecraft.getMinecraft().currentScreen = this;
	}

}
