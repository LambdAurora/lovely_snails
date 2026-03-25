/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.registry;

import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import dev.lambdaurora.lovely_snails.item.SnailSpawnEggItem;
import dev.lambdaurora.lovely_snails.network.SnailScreenHandlerPayload;
import dev.lambdaurora.lovely_snails.screen.SnailScreenHandler;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.Function;

import static dev.lambdaurora.lovely_snails.LovelySnails.id;

/**
 * Represents the Lovely Snails' registry.
 *
 * @author LambdAurora
 * @version 1.3.0
 * @since 1.0.0
 */
public final class LovelySnailsRegistry {
	private LovelySnailsRegistry() {
		throw new UnsupportedOperationException("Someone tried to instantiate a class only containing static definitions. How?");
	}

	/* Items */

	public static final SpawnEggItem SNAIL_SPAWN_EGG_ITEM;

	/* Screen handlers */

	public static final MenuType<SnailScreenHandler> SNAIL_SCREEN_HANDLER_TYPE =
			Registry.register(BuiltInRegistries.MENU, id("snail"), new ExtendedMenuType<>(
					SnailScreenHandler::new, SnailScreenHandlerPayload.STREAM_CODEC
			));

	/* Entities */

	public static final EntityType<SnailEntity> SNAIL_ENTITY_TYPE = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("snail"),
			FabricEntityType.Builder.createMob(
							SnailEntity::new, MobCategory.CREATURE, builder ->
									builder.defaultAttributes(SnailEntity::createSnailAttributes)
											.spawnPlacement(
													SpawnPlacementTypes.NO_RESTRICTIONS,
													Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
													SnailEntity::canSpawn
											)
					)
					.sized(1.5f, 2.f)
					.eyeHeight(1.f)
					.passengerAttachments(2.15f)
					.build(ResourceKey.create(Registries.ENTITY_TYPE, id("snail")))
	);

	/* Sounds */

	public static final SoundEvent SNAIL_DEATH_SOUND_EVENT = registerSound("entity.lovely_snails.snail.death");
	public static final SoundEvent SNAIL_HURT_SOUND_EVENT = registerSound("entity.lovely_snails.snail.hurt");

	/* Tags */

	public static final TagKey<Block> SNAIL_SPAWN_BLOCKS = TagKey.create(Registries.BLOCK, id("snail_spawn_blocks"));
	public static final TagKey<Item> SNAIL_BREEDING_ITEMS = TagKey.create(Registries.ITEM, id("snail_breeding_items"));
	public static final TagKey<Item> SNAIL_FOOD_ITEMS = TagKey.create(Registries.ITEM, id("snail_food_items"));
	public static final TagKey<Item> SNAIL_SCARY_ITEMS = TagKey.create(Registries.ITEM, id("snail_scary_items"));
	public static final TagKey<Biome> SNAIL_REGULAR_SPAWN_BIOMES = TagKey.create(Registries.BIOME, id("snail_spawn"));
	public static final TagKey<Biome> SNAIL_SWAMP_LIKE_SPAWN_BIOMES = TagKey.create(Registries.BIOME, id("swamp_like_spawn"));

	private static <T extends Item> T register(String name, Function<Item.Properties, T> item) {
		return Registry.register(BuiltInRegistries.ITEM, id(name), item.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(name)))));
	}

	private static SoundEvent registerSound(String path) {
		var id = id(path);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void init() {}

	static {
		SNAIL_SPAWN_EGG_ITEM = register("snail_spawn_egg",
				(properties) -> new SnailSpawnEggItem(SNAIL_ENTITY_TYPE, properties)
		);
	}
}
