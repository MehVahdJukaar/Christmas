package net.mehvahdjukaar.snowyspirit.integration.configured;


import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.integration.configured.CustomConfigSelectScreen;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.configs.ClientConfigs;
import net.mehvahdjukaar.snowyspirit.configs.CommonConfigs;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;
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
                ChatFormatting.AQUA+ "Snowy Spirit Configured",
                SnowySpirit.res("textures/blocks/gingerbread_frosted_block.png"),
                parent, ModConfigScreen::new, CommonConfigs.SPEC, ClientConfigs.SPEC);
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
        this.addRenderableWidget(LinkButton.create(this, centerX - 45 - 22, y, 3, 1, "https://www.patreon.com/user?u=53696377", "Support me on Patreon :D"));
        this.addRenderableWidget(LinkButton.create(this, centerX - 45 - 44, y, 2, 2, "https://ko-fi.com/mehvahdjukaar", "Donate a Coffee"));
        this.addRenderableWidget(LinkButton.create(this, centerX - 45 - 66, y, 1, 2, "https://www.curseforge.com/minecraft/mc-mods/snowy-spirit", "CurseForge Page"));
        this.addRenderableWidget(LinkButton.create(this, centerX - 45 - 88, y, 0, 2, "https://github.com/MehVahdJukaar/SnowySpirit", "Mod Wiki"));
        this.addRenderableWidget(LinkButton.create(this, centerX + 45 + 2, y, 1, 1, "https://discord.com/invite/qdKRTDf8Cv", "Mod Discord"));
        this.addRenderableWidget(LinkButton.create(this, centerX + 45 + 2 + 22, y, 0, 1, "https://www.youtube.com/watch?v=LSPNAtAEn28&t=1s", "Youtube Channel"));
        this.addRenderableWidget(LinkButton.create(this, centerX + 45 + 2 + 44, y, 2, 1, "https://twitter.com/Supplementariez?s=09", "Twitter Page"));
        this.addRenderableWidget(LinkButton.create(this, centerX + 45 + 2 + 66, y, 3, 2, "https://www.akliz.net/supplementaries", "Need a server? Get one with Akliz"));

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
