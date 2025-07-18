/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */


package dev.lambdaurora.lovely_snails.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.ticks.ContainerSingleItem;

/**
 * Represents a spawn egg that will try to sneak in where the spawn eggs are.
 *
 * @author Patbox
 * @version 1.2.1
 * @since 1.2.1
 */
public record EquipmentContainer(LivingEntity entity, EquipmentSlot slot) implements ContainerSingleItem {
    @Override
    public ItemStack getTheItem() {
        return entity.getItemBySlot(slot);
    }

    @Override
    public void setTheItem(ItemStack stack) {
        this.entity.setItemSlot(slot, stack);
    }

    @Override
    public void setChanged() {

    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.entity.isAlive();
    }
}
