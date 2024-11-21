package net.mehvahdjukaar.snowyspirit.mixins.neoforge;

import net.mehvahdjukaar.snowyspirit.common.block.GlowLightsBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.IShearable;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;
import java.util.List;

@Mixin(GlowLightsBlock.class)
public abstract class SelfGlowLightsBlockMixin extends Block implements IShearable {

    protected SelfGlowLightsBlockMixin(Properties arg) {
        super(arg);
    }

    @Shadow
    public abstract List<ItemStack> shearAction(@Nullable Player player, @Nonnull ItemStack item,
                                                Level world, BlockPos pos);

    @Override
    public boolean isShearable(@Nullable Player player, ItemStack item, Level level, BlockPos pos) {
        return true;
    }

    @Override
    public List<ItemStack> onSheared(@Nullable Player player, ItemStack item, Level level, BlockPos pos) {
        return shearAction(player, item, level, pos);
    }

}
