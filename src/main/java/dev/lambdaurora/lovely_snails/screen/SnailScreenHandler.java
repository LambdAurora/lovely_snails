/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.screen;

import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import dev.lambdaurora.lovely_snails.network.SnailScreenHandlerPayload;
import dev.lambdaurora.lovely_snails.network.SnailSetStoragePagePayload;
import dev.lambdaurora.lovely_snails.registry.LovelySnailsRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SnailScreenHandler extends AbstractContainerMenu implements ContainerListener {
	private final Player player;
	private final SimpleContainer inventory;
	private final SnailEntity entity;
	private final ChestSlot[] chestSlots = new ChestSlot[3];
	private final List<InventoryPageChangeListener> pageChangeListeners = new ArrayList<>();
	private int currentStoragePage;

	public SnailScreenHandler(int syncId, Inventory playerInventory, SnailScreenHandlerPayload payload) {
		this(syncId, playerInventory,
				playerInventory.player.level().getEntity(payload.snailId()) instanceof SnailEntity snail ? snail : null,
				payload.storagePage()
		);
	}

	public SnailScreenHandler(int syncId, Inventory playerInventory, SnailEntity snail, int currentStoragePage) {
		this(syncId, playerInventory, new SimpleContainer(snail.getInventorySize()), snail, currentStoragePage);
	}

	public SnailScreenHandler(int syncId, Inventory playerInventory, SimpleContainer inventory, SnailEntity entity, int currentStoragePage) {
		super(LovelySnailsRegistry.SNAIL_SCREEN_HANDLER_TYPE, syncId);
		checkContainerSize(inventory, entity.getInventorySize());
		this.player = playerInventory.player;
		this.inventory = inventory;
		this.entity = entity;
		this.currentStoragePage = currentStoragePage;

		inventory.onOpen(playerInventory.player);
		this.inventory.addListener(this);

		this.addSlot(new SaddleSlot(inventory, SnailEntity.SADDLE_SLOT, 26, 18));
		this.addSlot(new DecorSlot(inventory, SnailEntity.CARPET_SLOT, 26, 36));
		this.addSlot(this.chestSlots[0] = new ChestSlot(inventory, SnailEntity.FIRST_CHEST_SLOT, 8, 18, 0));
		this.addSlot(this.chestSlots[1] = new ChestSlot(inventory, SnailEntity.SECOND_CHEST_SLOT, 8, 36, 1));
		this.addSlot(this.chestSlots[2] = new ChestSlot(inventory, SnailEntity.THIRD_CHEST_SLOT, 8, 54, 2));

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

	public SimpleContainer getInventory() {
		return this.inventory;
	}

	/**
	 * Returns whether this snail holds an ender chest.
	 *
	 * @return {@code true} if this snails holds an ender chest, else {@code false}
	 */
	public boolean hasEnderChest() {
		for (int i = 2; i < 5; i++) {
			if (this.inventory.getItem(i).is(Items.ENDER_CHEST))
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
		return this.inventory.getItem(2 + page).is(Items.CHEST);
	}

	/**
	 * Returns whether there is items in the specified storage page.
	 *
	 * @param page the storage page
	 * @return {@code true} if there is items, else {@code false}
	 */
	public boolean hasItemsInStoragePage(int page) {
		for (int slot = 5 + page * 15; slot < 5 + page * 15 + 15; slot++) {
			if (!this.inventory.getItem(slot).isEmpty())
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
		if (this.player instanceof ServerPlayer serverPlayerEntity) {
			ServerPlayNetworking.send(serverPlayerEntity, new SnailSetStoragePagePayload(this.syncId, (byte) page));
		}

		for (var listener : this.pageChangeListeners) {
			listener.onCurrentPageSet(page);
		}
	}

	/**
	 * Returns which page should be selected on opening of the given inventory.
	 *
	 * @param inventory the inventory
	 * @return the page to select
	 */
	public static int getOpeningStoragePage(Container inventory) {
		for (int page = 0; page < 3; page++) {
			if (inventory.getItem(2 + page).is(Items.CHEST)) {
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
	public boolean stillValid(Player player) {
		return !this.entity.isInventoryDifferent(this.inventory)
				&& this.inventory.stillValid(player)
				&& this.entity.isAlive()
				&& this.entity.distanceTo(player) < 8.f;
	}

	private boolean attemptToTransferSlotToCurrentPage(ItemStack currentStack) {
		int page = this.getCurrentStoragePage();
		return this.moveItemStackTo(currentStack, 5 + page * 15, 5 + page * 15 + 15, false);
	}

	private @Nullable ItemStack attemptToTransferSlotToChestSlots(ItemStack currentStack) {
		for (int i = 0; i < this.chestSlots.length; i++) {
			int slot = SnailEntity.FIRST_CHEST_SLOT + i;

			if (this.chestSlots[i].mayPlace(currentStack) && !this.chestSlots[i].hasItem()
					&& !this.moveItemStackTo(currentStack, slot, slot + 1, false)) {
				return ItemStack.EMPTY;
			}
		}

		return null;
	}

	private @Nullable ItemStack attemptToTransferToSnail(Player player, ItemStack currentStack) {
		if (!this.snail().canUseSnail(player)) return null;

		ItemStack chestResult;

		if ((chestResult = this.attemptToTransferSlotToChestSlots(currentStack)) != null) {
			return chestResult;
		} else if (this.getSlot(SnailEntity.CARPET_SLOT).mayPlace(currentStack) && !this.getSlot(SnailEntity.CARPET_SLOT).hasItem()) {
			if (!this.moveItemStackTo(currentStack, 1, 2, false)) {
				return ItemStack.EMPTY;
			}
		} else if (this.getSlot(SnailEntity.SADDLE_SLOT).mayPlace(currentStack)) {
			if (!this.moveItemStackTo(currentStack, 0, 1, false)) {
				return ItemStack.EMPTY;
			}
		} else if (!this.attemptToTransferSlotToCurrentPage(currentStack)) {
			return ItemStack.EMPTY;
		}

		return null;
	}

	@Override
	public void clicked(int slotIndex, int button, ClickType actionType, Player player) {
		if (slotIndex < this.inventory.size() && !this.snail().canUseSnail(player))
			return;

		super.clicked(slotIndex, button, actionType, player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int fromIndex) {
		var stack = ItemStack.EMPTY;
		var slot = this.slots.get(fromIndex);

		if (slot.hasItem()) {
			var currentStack = slot.getItem();
			stack = currentStack.copy();
			int inventorySize = this.inventory.size();

			ItemStack insertionIntoSnail;

			if (fromIndex < inventorySize) {
				if (this.snail().canUseSnail(player) && !this.moveItemStackTo(currentStack, inventorySize, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if ((insertionIntoSnail = this.attemptToTransferToSnail(player, currentStack)) != null) {
				return insertionIntoSnail;
			} else {
				int playerInventoryEnd = inventorySize + 27;
				int hotbarEnd = playerInventoryEnd + 9;
				if (fromIndex >= playerInventoryEnd && fromIndex < hotbarEnd) {
					if (!this.moveItemStackTo(currentStack, inventorySize, playerInventoryEnd, false)) {
						return ItemStack.EMPTY;
					}
				} else if (fromIndex < playerInventoryEnd) {
					if (!this.moveItemStackTo(currentStack, playerInventoryEnd, hotbarEnd, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.moveItemStackTo(currentStack, playerInventoryEnd, playerInventoryEnd, false)) {
					return ItemStack.EMPTY;
				}

				return ItemStack.EMPTY;
			}

			if (currentStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (currentStack.getCount() == stack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, currentStack);
		}

		return stack;
	}

	@Override
	public boolean onButtonClick(Player player, int id) {
		if (id == 0 && this.hasEnderChest()) {
			this.snail().openEnderChestInventory(player);
			return true;
		}
		return super.onButtonClick(player, id);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.inventory.onClose(player);
		this.inventory.removeListener(this);
	}

	@Override
	public void onContainerChanged(Container sender) {
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

	private class SnailSlot extends Slot {
		public SnailSlot(Container inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean mayPickup(Player playerEntity) {
			return this.snail().canUseSnail(playerEntity);
		}

		protected SnailScreenHandler screenHandler() {
			return SnailScreenHandler.this;
		}

		protected SnailEntity snail() {
			return this.screenHandler().snail();
		}
	}

	private class SaddleSlot extends SnailSlot {
		public SaddleSlot(Container inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return stack.is(Items.SADDLE) && !this.hasItem() && this.isEnabled();
		}

		@Override
		public boolean isEnabled() {
			return this.snail().isSaddleable();
		}
	}

	private class DecorSlot extends SnailSlot {
		public DecorSlot(Container inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return SnailEntity.getColorFromCarpet(stack) != null;
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}
	}

	private class ChestSlot extends SnailSlot {
		private final int storagePage;

		public ChestSlot(Container inventory, int index, int x, int y, int storagePage) {
			super(inventory, index, x, y);
			this.storagePage = storagePage;
		}

		@Override
		public boolean isEnabled() {
			return !this.snail().isBaby();
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return (stack.is(Items.CHEST) || stack.is(Items.ENDER_CHEST)) && this.isEnabled();
		}

		@Override
		public boolean mayPickup(Player player) {
			return super.mayPickup(player) && !this.screenHandler().hasItemsInStoragePage(this.storagePage);
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}
	}

	private class StorageSlot extends SnailSlot {
		private final int storagePage;

		public StorageSlot(Container inventory, int index, int x, int y, int storagePage) {
			super(inventory, index, x, y);
			this.storagePage = storagePage;
		}

		@Override
		public boolean isEnabled() {
			return this.screenHandler().hasChest(this.storagePage) && this.screenHandler().currentStoragePage == this.storagePage;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return this.isEnabled();
		}
	}
}
