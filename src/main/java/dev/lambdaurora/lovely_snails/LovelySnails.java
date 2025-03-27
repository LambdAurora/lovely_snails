/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails;

import dev.lambdaurora.lovely_snails.network.SnailSetStoragePagePayload;
import dev.lambdaurora.lovely_snails.registry.LovelySnailsRegistry;
import dev.lambdaurora.lovely_snails.screen.SnailScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;

/**
 * Represents the Lovely Snails mod.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
public class LovelySnails implements ModInitializer {
	public static final String NAMESPACE = "lovely_snails";

	@Override
	public void onInitialize() {
		LovelySnailsRegistry.init();

		ServerPlayNetworking.registerGlobalReceiver(SnailSetStoragePagePayload.TYPE,
				(payload, context) -> {
					context.server().execute(() -> {
						if (context.player().containerMenu instanceof SnailScreenHandler snailScreenHandler
								&& snailScreenHandler.syncId == payload.syncId()) {
							snailScreenHandler.setCurrentStoragePage(payload.storagePage());
						}
					});
				});

		BiomeModifications.addSpawn(BiomeSelectors.tag(LovelySnailsRegistry.SNAIL_SWAMP_LIKE_SPAWN_BIOMES),
				MobCategory.CREATURE, LovelySnailsRegistry.SNAIL_ENTITY_TYPE, 10, 1, 3
		);
		BiomeModifications.addSpawn(BiomeSelectors.tag(LovelySnailsRegistry.SNAIL_REGULAR_SPAWN_BIOMES),
				MobCategory.CREATURE, LovelySnailsRegistry.SNAIL_ENTITY_TYPE, 8, 1, 3
		);
	}

	public static Identifier id(String path) {
		return Identifier.of(NAMESPACE, path);
	}

	public static void readInventoryNbt(
			HolderLookup.Provider registryLookup, NbtCompound nbt, String key, Container stacks, int start
	) {
		var inventoryNbt = nbt.getList(key, NbtElement.COMPOUND_TYPE);

		for (int i = 0; i < inventoryNbt.size(); ++i) {
			var slotNbt = inventoryNbt.getCompound(i);
			int slotId = slotNbt.getByte("slot") & 255;
			if (slotId < stacks.size()) {
				stacks.setItem(start + slotId, ItemStack.parseOptional(registryLookup, slotNbt));
			}
		}
	}

	public static void writeInventoryNbt(
			HolderLookup.Provider registryLookup, NbtCompound nbt, String key, Container stacks, int start, int end
	) {
		writeInventoryNbt(registryLookup, nbt, key, stacks, start, end, true);
	}

	public static void writeInventoryNbt(
			HolderLookup.Provider registryLookup, NbtCompound nbt, String key, Container stacks, int start, int end, boolean setIfEmpty
	) {
		var inventoryNbt = new NbtList();

		for (int i = start; i < end; ++i) {
			var slotStack = stacks.getItem(i);
			if (!slotStack.isEmpty()) {
				var slotNbt = new NbtCompound();
				slotNbt.putByte("slot", (byte) (i - start));
				inventoryNbt.add(slotStack.save(registryLookup, slotNbt));
			}
		}

		if (!inventoryNbt.isEmpty() || setIfEmpty) {
			nbt.put(key, inventoryNbt);
		}
	}
}
