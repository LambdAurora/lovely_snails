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
import dev.lambdaurora.lovely_snails.screen.SnailScreenHandler;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;

import static dev.lambdaurora.lovely_snails.LovelySnails.id;

/**
 * Represents the Lovely Snails' registry.
 *
 * @author LambdAurora
 * @version 1.1.1
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
			Registry.register(BuiltInRegistries.MENU, id("snail"), new ExtendedScreenHandlerType<>(SnailScreenHandler::new));

	/* Entities */

	public static final EntityType<SnailEntity> SNAIL_ENTITY_TYPE = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("snail"),
			FabricEntityTypeBuilder.<SnailEntity>createMob()
					.spawnGroup(MobCategory.CREATURE)
					.entityFactory(SnailEntity::new)
					.defaultAttributes(SnailEntity::createSnailAttributes)
					.dimensions(EntityDimensions.scalable(1.5f, 2.f))
					.spawnRestriction(SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
							SnailEntity::canSpawn)
					.build()
	);

	/* Sounds */

	public static final SoundEvent SNAIL_DEATH_SOUND_EVENT = registerSound("entity.lovely_snails.snail.death");
	public static final SoundEvent SNAIL_HURT_SOUND_EVENT = registerSound("entity.lovely_snails.snail.hurt");

	/* Packet */

	public static final Identifier SNAIL_SET_STORAGE_PAGE = id("snail_set_storage_page");

	/* Tags */

	public static final TagKey<Block> SNAIL_SPAWN_BLOCKS = TagKey.of(Registries.BLOCK, id("snail_spawn_blocks"));
	public static final TagKey<Item> SNAIL_BREEDING_ITEMS = TagKey.of(Registries.ITEM, id("snail_breeding_items"));
	public static final TagKey<Item> SNAIL_FOOD_ITEMS = TagKey.of(Registries.ITEM, id("snail_food_items"));
	public static final TagKey<Biome> SNAIL_REGULAR_SPAWN_BIOMES = TagKey.of(Registries.BIOME, id("snail_spawn"));
	public static final TagKey<Biome> SNAIL_SWAMP_LIKE_SPAWN_BIOMES = TagKey.of(Registries.BIOME, id("swamp_like_spawn"));

	private static <T extends Item> T register(String name, T item) {
		return Registry.register(BuiltInRegistries.ITEM, id(name), item);
	}

	private static SoundEvent registerSound(String path) {
		var id = id(path);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void init() {}

	static {
		SNAIL_SPAWN_EGG_ITEM = register("snail_spawn_egg", new SnailSpawnEggItem(SNAIL_ENTITY_TYPE, 0xff36201c, 0xffd58d51,
				new FabricItemSettings()));
	}
}
