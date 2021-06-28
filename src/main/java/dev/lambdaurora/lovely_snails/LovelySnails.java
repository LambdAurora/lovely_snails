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

package dev.lambdaurora.lovely_snails;

import dev.lambdaurora.lovely_snails.registry.LovelySnailsRegistry;
import dev.lambdaurora.lovely_snails.screen.SnailScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

/**
 * Represents the Lovely Snails mod.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LovelySnails implements ModInitializer {
    public static final String NAMESPACE = "lovely_snails";

    @Override
    public void onInitialize() {
        LovelySnailsRegistry.init();

        ServerPlayNetworking.registerGlobalReceiver(LovelySnailsRegistry.SNAIL_SET_STORAGE_PAGE,
                (server, player, handler, buf, responseSender) -> {
                    int syncId = buf.readVarInt();
                    byte storagePage = buf.readByte();
                    server.execute(() -> {
                        if (handler.getPlayer().currentScreenHandler instanceof SnailScreenHandler snailScreenHandler
                                && snailScreenHandler.syncId == syncId) {
                            snailScreenHandler.setCurrentStoragePage(storagePage);
                        }
                    });
                });
    }

    public static Identifier id(String path) {
        return new Identifier(NAMESPACE, path);
    }

    public static void readInventoryNbt(NbtCompound nbt, String key, Inventory stacks, int start) {
        var inventoryNbt = nbt.getList(key, NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < inventoryNbt.size(); ++i) {
            var slotNbt = inventoryNbt.getCompound(i);
            int slotId = slotNbt.getByte("slot") & 255;
            if (slotId < stacks.size()) {
                stacks.setStack(start + slotId, ItemStack.fromNbt(slotNbt));
            }
        }
    }

    public static NbtCompound writeInventoryNbt(NbtCompound nbt, String key, Inventory stacks, int start, int end) {
        return writeInventoryNbt(nbt, key, stacks, start, end, true);
    }

    public static NbtCompound writeInventoryNbt(NbtCompound nbt, String key, Inventory stacks, int start, int end, boolean setIfEmpty) {
        var inventoryNbt = new NbtList();

        for (int i = start; i < end; ++i) {
            var slotStack = stacks.getStack(i);
            if (!slotStack.isEmpty()) {
                var slotNbt = new NbtCompound();
                slotNbt.putByte("slot", (byte) (i - start));
                slotStack.writeNbt(slotNbt);
                inventoryNbt.add(slotNbt);
            }
        }

        if (!inventoryNbt.isEmpty() || setIfEmpty) {
            nbt.put(key, inventoryNbt);
        }

        return nbt;
    }
}
