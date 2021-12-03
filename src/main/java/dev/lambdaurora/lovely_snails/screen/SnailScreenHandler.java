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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class SnailScreenHandler extends ScreenHandler implements InventoryChangedListener {
	private final PlayerEntity player;
	private final SimpleInventory inventory;
	private final SnailEntity entity;
	private final List<InventoryPageChangeListener> pageChangeListeners = new ArrayList<>();
	private int currentStoragePage;

	public SnailScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
		this(syncId, playerInventory,
				playerInventory.player.getWorld().getEntityById(buf.readVarInt()) instanceof SnailEntity snail ? snail : null,
				buf.readByte()
		);
	}

	public SnailScreenHandler(int syncId, PlayerInventory playerInventory, SnailEntity snail, int currentStoragePage) {
		this(syncId, playerInventory, new SimpleInventory(snail.getInventorySize()), snail, currentStoragePage);
	}

	public SnailScreenHandler(int syncId, PlayerInventory playerInventory, SimpleInventory inventory, SnailEntity entity, int currentStoragePage) {
		super(LovelySnailsRegistry.SNAIL_SCREEN_HANDLER_TYPE, syncId);
		checkSize(inventory, entity.getInventorySize());
		this.player = playerInventory.player;
		this.inventory = inventory;
		this.entity = entity;
		this.currentStoragePage = currentStoragePage;

		inventory.onOpen(playerInventory.player);
		this.inventory.addListener(this);

		this.addSlot(new SaddleSlot(inventory, SnailEntity.SADDLE_SLOT, 26, 18, entity));
		this.addSlot(new DecorSlot(inventory, SnailEntity.CARPET_SLOT, 26, 36));
		this.addSlot(new ChestSlot(inventory, 2, 8, 18, 0));
		this.addSlot(new ChestSlot(inventory, 3, 8, 36, 1));
		this.addSlot(new ChestSlot(inventory, 4, 8, 54, 2));

		for (int page = 0; page < 3; page++) {
			for (int row = 0; row < 3; row++) {
				for (int column = 0; column < 5; column++) {
					this.addSlot(new StorageSlot(inventory, 5 + page * 15 + column + row * 5,
							80 + 19 + column * 18, 18 + row * 18, page));
				}
			}
		}

		// Player inventory.
		for (int row = 0; row < 3; ++row) {
			for (int column = 0; column < 9; ++column) {
				this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 27 + column * 18, 102 + row * 18 + -18));
			}
		}

		for (int column = 0; column < 9; ++column) {
			this.addSlot(new Slot(playerInventory, column, 27 + column * 18, 142));
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

	public SimpleInventory getInventory() {
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

	/**
	 * Returns whether this snail has any chest to expand storage.
	 *
	 * @return {@code true} if this snail has any chest, else {@code false}
	 */
	public boolean hasChests() {
		for (int i = 0; i < 3; i++) {
			if (this.hasChest(i))
				return true;
		}
		return false;
	}

	/**
	 * Returns whether this snail has a chest for the given storage page.
	 *
	 * @param page the storage page
	 * @return {@code true} if there is a chest for the given storage page, else {@code false}
	 */
	public boolean hasChest(int page) {
		return this.inventory.getStack(2 + page).isOf(Items.CHEST);
	}

	/**
	 * Returns whether there is items in the specified storage page.
	 *
	 * @param page the storage page
	 * @return {@code true} if there is items, else {@code false}
	 */
	public boolean hasItemsInStoragePage(int page) {
		for (int slot = 5 + page * 15; slot < 5 + page * 15 + 15; slot++) {
			if (!this.inventory.getStack(slot).isEmpty())
				return true;
		}
		return false;
	}

	/**
	 * Returns the current storage page.
	 *
	 * @return the storage page
	 */
	public int getCurrentStoragePage() {
		return this.currentStoragePage;
	}

	public void setCurrentStoragePage(int page) {
		this.currentStoragePage = page;
		if (this.player instanceof ServerPlayerEntity serverPlayerEntity) {
			var buffer = PacketByteBufs.create();
			buffer.writeVarInt(this.syncId);
			buffer.writeByte(page);
			ServerPlayNetworking.send(serverPlayerEntity, LovelySnailsRegistry.SNAIL_SET_STORAGE_PAGE, buffer);
		}

		for (var listener : this.pageChangeListeners) {
			listener.onCurrentPageSet(page);
		}
	}

	/**
	 * Requests the server to switch to the given storage page.
	 *
	 * @param page the storage page to switch to
	 */
	@Environment(EnvType.CLIENT)
	public void requestStoragePage(int page) {
		var buffer = PacketByteBufs.create();
		buffer.writeVarInt(this.syncId);
		buffer.writeByte(page);
		ClientPlayNetworking.send(LovelySnailsRegistry.SNAIL_SET_STORAGE_PAGE, buffer);
	}

	/**
	 * Returns which page should be selected on opening of the given inventory.
	 *
	 * @param inventory the inventory
	 * @return the page to select
	 */
	public static int getOpeningStoragePage(Inventory inventory) {
		for (int page = 0; page < 3; page++) {
			if (inventory.getStack(2 + page).isOf(Items.CHEST)) {
				return page;
			}
		}
		return 0;
	}

	public void addPageChangeListener(InventoryPageChangeListener listener) {
		this.pageChangeListeners.add(listener);
	}

	public void removePageChangeListener(InventoryPageChangeListener listener) {
		this.pageChangeListeners.remove(listener);
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return !this.entity.isInventoryDifferent(this.inventory)
				&& this.inventory.canPlayerUse(player)
				&& this.entity.isAlive()
				&& this.entity.distanceTo(player) < 8.f;
	}

	private boolean attemptToTransferSlotToCurrentPage(ItemStack currentStack) {
		int page = this.getCurrentStoragePage();
		return this.insertItem(currentStack, 5 + page * 15, 5 + page * 15 + 15, false);
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
			} else if (this.getSlot(2).canInsert(currentStack) && !this.getSlot(2).hasStack()) {
				if (!this.insertItem(currentStack, 2, 3, false)) {
					return ItemStack.EMPTY;
				}
			} else if (this.getSlot(3).canInsert(currentStack) && !this.getSlot(3).hasStack()) {
				if (!this.insertItem(currentStack, 3, 4, false)) {
					return ItemStack.EMPTY;
				}
			} else if (this.getSlot(4).canInsert(currentStack) && !this.getSlot(4).hasStack()) {
				if (!this.insertItem(currentStack, 4, 5, false)) {
					return ItemStack.EMPTY;
				}
			} else if (this.getSlot(SnailEntity.CARPET_SLOT).canInsert(currentStack) && !this.getSlot(SnailEntity.CARPET_SLOT).hasStack()) {
				if (!this.insertItem(currentStack, 1, 2, false)) {
					return ItemStack.EMPTY;
				}
			} else if (this.getSlot(SnailEntity.SADDLE_SLOT).canInsert(currentStack)) {
				if (!this.insertItem(currentStack, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.attemptToTransferSlotToCurrentPage(currentStack)) {
				int end = inventorySize + 27;
				int m = end + 9;
				if (index >= end && index < m) {
					if (!this.insertItem(currentStack, inventorySize, end, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index < end) {
					if (!this.insertItem(currentStack, end, m, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.insertItem(currentStack, end, end, false)) {
					return ItemStack.EMPTY;
				}

				return ItemStack.EMPTY;
			}

			if (currentStack.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}

			if (currentStack.getCount() == stack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTakeItem(player, currentStack);
		}

		return stack;
	}

	@Override
	public boolean onButtonClick(PlayerEntity player, int id) {
		if (id == 0 && this.hasEnderChest()) {
			this.snail().openEnderChestInventory(player);
			return true;
		}
		return super.onButtonClick(player, id);
	}

	@Override
	public void close(PlayerEntity playerEntity) {
		super.close(playerEntity);
		this.inventory.onClose(playerEntity);
		this.inventory.removeListener(this);
	}

	@Override
	public void onInventoryChanged(Inventory sender) {
		if (this.hasChests() && !this.hasChest(this.currentStoragePage)) {
			this.currentStoragePage = switch (this.currentStoragePage) {
				case 2 -> {
					if (this.hasChest(1))
						yield 1;
					else
						yield 0;
				}
				default -> getOpeningStoragePage(this.getInventory());
			};

			for (var listener : this.pageChangeListeners) {
				listener.onCurrentPageSet(this.currentStoragePage);
			}
		}
	}

	public interface InventoryPageChangeListener {
		void onCurrentPageSet(int page);
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
		public boolean isEnabled() {
			return true;
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return SnailEntity.getColorFromCarpet(stack) != null;
		}

		@Override
		public int getMaxItemCount() {
			return 1;
		}
	}

	private class ChestSlot extends Slot {
		private final int storagePage;

		public ChestSlot(Inventory inventory, int index, int x, int y, int storagePage) {
			super(inventory, index, x, y);
			this.storagePage = storagePage;
		}

		@Override
		public boolean isEnabled() {
			return !SnailScreenHandler.this.snail().isBaby();
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return (stack.isOf(Items.CHEST) || stack.isOf(Items.ENDER_CHEST)) && this.isEnabled();
		}

		@Override
		public boolean canTakeItems(PlayerEntity playerEntity) {
			return super.canTakeItems(playerEntity) && !SnailScreenHandler.this.hasItemsInStoragePage(this.storagePage);
		}

		@Override
		public int getMaxItemCount() {
			return 1;
		}
	}

	private class StorageSlot extends Slot {
		private final int storagePage;

		public StorageSlot(Inventory inventory, int index, int x, int y, int storagePage) {
			super(inventory, index, x, y);
			this.storagePage = storagePage;
		}

		@Override
		public boolean isEnabled() {
			return SnailScreenHandler.this.hasChest(this.storagePage) && SnailScreenHandler.this.currentStoragePage == this.storagePage;
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return this.isEnabled();
		}
	}
}
