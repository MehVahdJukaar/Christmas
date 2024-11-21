package net.mehvahdjukaar.snowyspirit.integration.configured;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.configured.client.util.ScreenUtil;
import net.mehvahdjukaar.moonlight.api.client.gui.MediaButton;
import net.mehvahdjukaar.moonlight.api.integration.configured.CustomConfigSelectScreen;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.configs.ClientConfigs;
import net.mehvahdjukaar.snowyspirit.configs.CommonConfigs;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class ModConfigSelectScreen extends CustomConfigSelectScreen {


    public ModConfigSelectScreen(Screen parent) {
        super(SnowySpirit.MOD_ID, ModRegistry.WREATH.get().asItem().getDefaultInstance(),
                ChatFormatting.AQUA+ "Snowy Spirit Configured", parent,
                ModConfigScreen::new, CommonConfigs.SPEC, ClientConfigs.SPEC);
    }


    @Override
    protected void init() {
        super.init();
        Button found = null;
        for (var c : this.children()) {
            if (c instanceof Button button) {
                if (button.getWidth() == 150) found = button;
            }
        }
        if (found != null) this.removeWidget(found);

        int y = this.height - 29;
        int centerX = this.width / 2;
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (buttonx) -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(centerX - 45, y, 90, 20).build());
        this.addRenderableWidget(MediaButton.patreon(this, centerX - 45 - 22, y,
                "https://www.patreon.com/user?u=53696377"));

        this.addRenderableWidget(MediaButton.koFi(this, centerX - 45 - 22 * 2, y,
                "https://ko-fi.com/mehvahdjukaar"));

        this.addRenderableWidget(MediaButton.curseForge(this, centerX - 45 - 22 * 3, y,
                "https://www.curseforge.com/minecraft/mc-mods/snowy-spirit"));

        this.addRenderableWidget(MediaButton.github(this, centerX - 45 - 22 * 4, y,
                "https://github.com/MehVahdJukaar/snowyspirit/wiki"));


        this.addRenderableWidget(MediaButton.discord(this, centerX + 45 + 2, y,
                "https://discord.com/invite/qdKRTDf8Cv"));

        this.addRenderableWidget(MediaButton.youtube(this, centerX + 45 + 2 + 22, y,
                "https://www.youtube.com/watch?v=LSPNAtAEn28&t=1s"));

        this.addRenderableWidget(MediaButton.twitter(this, centerX + 45 + 2 + 22 * 2, y,
                "https://twitter.com/Supplementariez?s=09"));

        this.addRenderableWidget(MediaButton.akliz(this, centerX + 45 + 2 + 22 * 3, y,
                "https://www.akliz.net/supplementaries"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        var level = Minecraft.getInstance().level;
        if(level != null && SnowySpirit.isChristmasSeason(level)) {
            int x = (int) (this.width * 0.93f);
            graphics.renderFakeItem(Items.SNOWBALL.getDefaultInstance(), x, 16);
            if (ScreenUtil.isMouseWithin(x , 16, 16, 16, mouseX, mouseY)) {
                graphics.renderTooltip(this.font, this.font.split(Component.translatable("gui.snowyspirit.snow_season_on").withStyle(ChatFormatting.AQUA), 200), mouseX, mouseY);
            }
        }
    }
}
