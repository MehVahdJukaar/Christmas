package net.mehvahdjukaar.snowyspirit.reg;

import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.snowyspirit.common.ai.WinterVillagerAI;
import net.mehvahdjukaar.snowyspirit.common.entity.SledEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class ModSetup {


    public static void setup() {

        WinterVillagerAI.setup();
        registerBurnTimes();
        ModRegistry.SLED_ITEMS.forEach((key, value) ->
                DispenserBlock.registerBehavior(value, new SledDispenserBehavior(key)));

    }


    private static void registerBurnTimes() {
        for (var b : ModRegistry.GLOW_LIGHTS_BLOCKS.values()) {
            RegHelper.registerBlockFlammability(b.get(), 30, 60);
        }
        RegHelper.registerBlockFlammability(ModRegistry.GINGER_WILD.get(), 60, 100);
    }


    public static class SledDispenserBehavior extends DefaultDispenseItemBehavior {
        private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
        private final WoodType type;

        public SledDispenserBehavior(WoodType pType) {
            this.type = pType;
        }

        @Override
        public ItemStack execute(BlockSource pSource, ItemStack pStack) {
            Direction direction = pSource.state().getValue(DispenserBlock.FACING);
            Level level = pSource.level();
            var center = pSource.center();
            double d0 = center.x() + (direction.getStepX() * 1.2F);
            double d1 = center.y() + (direction.getStepY() * 1.2F);
            double d2 = center.z() + (direction.getStepZ() * 1.2F);
            BlockPos blockpos = pSource.pos().relative(direction);

            if (!level.getBlockState(blockpos).isAir() || !level.getFluidState(blockpos).isEmpty()) {
                return this.defaultDispenseItemBehavior.dispense(pSource, pStack);
            }


            SledEntity sled = new SledEntity(level, d0, d1, d2);
            sled.setWoodType(this.type);
            sled.setYRot(direction.toYRot());
            level.addFreshEntity(sled);
            pStack.shrink(1);
            return pStack;
        }

        @Override
        protected void playSound(BlockSource pSource) {
            pSource.level().levelEvent(1000, pSource.pos(), 0);
        }
    }
}
