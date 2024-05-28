package net.mehvahdjukaar.snowyspirit.integration.supp;

import net.mehvahdjukaar.snowyspirit.common.entity.ContainerHolderEntity;
import net.mehvahdjukaar.supplementaries.common.inventories.SackContainerMenu;
import net.mehvahdjukaar.supplementaries.common.items.CandyItem;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class SuppCompat {

    //disbled
    public static boolean isSack(Item i) {
        return false;
    }

    public static AbstractContainerMenu createSackMenu(int id, Inventory inventory, ContainerHolderEntity entity) {
        return new SackContainerMenu(id, inventory);
    }

    //TODO: add
    public static void triggerSweetTooth(Level level, LivingEntity entity) {
        CandyItem.increaseSweetTooth(level, entity, 8 * 20);
    }
}
