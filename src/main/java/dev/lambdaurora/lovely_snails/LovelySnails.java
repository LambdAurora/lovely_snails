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
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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

	public static void readInventory(ValueInput input, String key, Container stacks, int start) {
		var slots = input.list(key, ItemStackWithSlot.CODEC);
		if (slots.isEmpty()) return;


		for (var slot : slots.get()) {
			if (slot.isValidInContainer(stacks.size() - start)) {
				stacks.setItem(start + slot.slot(), slot.stack());
			}
		}
	}

	public static void writeInventory(ValueOutput output, String key, Container stacks, int start, int end) {
		writeInventory(output, key, stacks, start, end, true);
	}

	public static void writeInventory(ValueOutput output, String key, Container stacks, int start, int end, boolean setIfEmpty) {
		var list = output.list(key, ItemStackWithSlot.CODEC);

		for (int i = start; i < end; ++i) {
			var slotStack = stacks.getItem(i);
			if (!slotStack.isEmpty()) {
				list.add(new ItemStackWithSlot(i - start, slotStack));
			}
		}

		if (list.isEmpty() && !setIfEmpty) {
			output.remove(key);
		}
	}
}
