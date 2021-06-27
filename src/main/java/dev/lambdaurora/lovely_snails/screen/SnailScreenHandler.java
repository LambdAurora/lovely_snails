/*
 * Copyright (c) 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.lambdaurora.lovely_snails.screen;

import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import dev.lambdaurora.lovely_snails.registry.LovelySnailsRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class SnailScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final SnailEntity entity;

    public SnailScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory,
                playerInventory.player.getEntityWorld().getEntityById(buf.readVarInt()) instanceof SnailEntity snail ? snail : null
        );
    }

    public SnailScreenHandler(int syncId, PlayerInventory playerInventory, SnailEntity snail) {
        this(syncId, playerInventory, new SimpleInventory(snail.getInventorySize()), snail);
    }

    public SnailScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, SnailEntity entity) {
        super(LovelySnailsRegistry.SNAIL_SCREEN_HANDLER_TYPE, syncId);
        checkSize(inventory, entity.getInventorySize());
        this.inventory = inventory;
        this.entity = entity;

        inventory.onOpen(playerInventory.player);

        this.addSlot(new SaddleSlot(inventory, SnailEntity.SADDLE_SLOT, 8, 18, entity));
        this.addSlot(new DecorSlot(inventory, SnailEntity.CARPET_SLOT, 8, 36));
        this.addSlot(new ChestSlot(inventory, 2, -10, 18));
        this.addSlot(new ChestSlot(inventory, 3, -10, 36));
        this.addSlot(new ChestSlot(inventory, 4, -10, 54));

        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 102 + row * 18 + -18));
            }
        }

        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    /**
     * Returns the associated snail entity.
     *
     * @return the snail entity
     */
    public SnailEntity snail() {
        return this.entity;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Returns whether this snail holds an ender chest.
     *
     * @return {@code true} if this snails holds an ender chest, else {@code false}
     */
    public boolean hasEnderChest() {
        for (int i = 2; i < 5; i++) {
            if (this.inventory.getStack(i).isOf(Items.ENDER_CHEST))
                return true;
        }
        return false;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return !this.entity.isInventoryDifferent(this.inventory)
                && this.inventory.canPlayerUse(player)
                && this.entity.isAlive()
                && this.entity.distanceTo(player) < 8.f;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        var stack = ItemStack.EMPTY;
        var slot = this.slots.get(index);
        if (slot.hasStack()) {
            var currentStack = slot.getStack();
            stack = currentStack.copy();
            int inventorySize = this.inventory.size();
            if (index < inventorySize) {
                if (!this.insertItem(currentStack, inventorySize, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(SnailEntity.CARPET_SLOT).canInsert(currentStack) && !this.getSlot(1).hasStack()) {
                if (!this.insertItem(currentStack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(SnailEntity.SADDLE_SLOT).canInsert(currentStack)) {
                if (!this.insertItem(currentStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (inventorySize <= 2 || !this.insertItem(currentStack, 2, inventorySize, false)) {
                int k = inventorySize + 27;
                int m = k + 9;
                if (index >= k && index < m) {
                    if (!this.insertItem(currentStack, inventorySize, k, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= inventorySize && index < k) {
                    if (!this.insertItem(currentStack, k, m, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.insertItem(currentStack, k, k, false)) {
                    return ItemStack.EMPTY;
                }

                return ItemStack.EMPTY;
            }

            if (currentStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return stack;
    }

    @Override
    public void close(PlayerEntity playerEntity) {
        super.close(playerEntity);
        this.inventory.onClose(playerEntity);
    }

    private static class SaddleSlot extends Slot {
        private final SnailEntity snail;

        public SaddleSlot(Inventory inventory, int index, int x, int y, SnailEntity snail) {
            super(inventory, index, x, y);
            this.snail = snail;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.isOf(Items.SADDLE) && !this.hasStack() && this.snail.canBeSaddled();
        }

        @Override
        public boolean isEnabled() {
            return this.snail.canBeSaddled();
        }
    }

    private static class DecorSlot extends Slot {
        public DecorSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return SnailEntity.getColorFromCarpet(stack) != null;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }
    }

    private static class ChestSlot extends Slot {
        public ChestSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.isOf(Items.CHEST) || stack.isOf(Items.ENDER_CHEST);
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }
    }
}
