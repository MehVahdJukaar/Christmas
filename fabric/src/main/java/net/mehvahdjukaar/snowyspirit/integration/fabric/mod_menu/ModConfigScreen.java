package net.mehvahdjukaar.snowyspirit.integration.fabric.mod_menu;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.gui.MediaButton;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.FabricConfigListScreen;
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

public class ModConfigScreen extends FabricConfigListScreen {

    public ModConfigScreen(Screen parent) {
        super(SnowySpirit.MOD_ID, ModRegistry.WREATH.get().asItem().getDefaultInstance(),
                Component.literal(ChatFormatting.AQUA + "Snowy Spirit Configs"),
                SnowySpirit.res("textures/blocks/gingerbread_frosted_block.png"),
                parent, CommonConfigs.SPEC, ClientConfigs.SPEC);
    }

    @Override
    protected void addExtraButtons() {

        int y = this.height - 27;
        int centerX = this.width / 2;
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (buttonx) -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(centerX - 45, y, 90, 20).build());

        //TODO
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        var level = Minecraft.getInstance().level;
        if (level != null && SnowySpirit.isChristmasSeason(level)) {
            int x = (int) (this.width * 0.93f);
            graphics.renderFakeItem(Items.SNOWBALL.getDefaultInstance(), x, 16);
        }
    }

}

