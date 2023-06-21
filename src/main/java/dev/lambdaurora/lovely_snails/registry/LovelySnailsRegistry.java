/*
 * Copyright (c) 2021 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.lovely_snails.registry;

import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import dev.lambdaurora.lovely_snails.item.SnailSpawnEggItem;
import dev.lambdaurora.lovely_snails.screen.SnailScreenHandler;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;

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

	public static final ScreenHandlerType<SnailScreenHandler> SNAIL_SCREEN_HANDLER_TYPE =
			Registry.register(Registries.SCREEN_HANDLER_TYPE, id("snail"), new ExtendedScreenHandlerType<>(SnailScreenHandler::new));

	/* Entities */

	public static final EntityType<SnailEntity> SNAIL_ENTITY_TYPE = Registry.register(Registries.ENTITY_TYPE, id("snail"),
			FabricEntityTypeBuilder.<SnailEntity>createMob()
					.spawnGroup(SpawnGroup.CREATURE)
					.entityFactory(SnailEntity::new)
					.defaultAttributes(SnailEntity::createSnailAttributes)
					.dimensions(EntityDimensions.changing(1.5f, 2.f))
					.spawnRestriction(SpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
							SnailEntity::canSpawn)
					.build()
	);

	/* Sounds */

	public static final SoundEvent SNAIL_DEATH_SOUND_EVENT = registerSound("entity.lovely_snails.snail.death");
	public static final SoundEvent SNAIL_HURT_SOUND_EVENT = registerSound("entity.lovely_snails.snail.hurt");

	/* Packet */

	public static final Identifier SNAIL_SET_STORAGE_PAGE = id("snail_set_storage_page");

	/* Tags */

	public static final TagKey<Block> SNAIL_SPAWN_BLOCKS = TagKey.of(RegistryKeys.BLOCK, id("snail_spawn_blocks"));
	public static final TagKey<Item> SNAIL_BREEDING_ITEMS = TagKey.of(RegistryKeys.ITEM, id("snail_breeding_items"));
	public static final TagKey<Item> SNAIL_FOOD_ITEMS = TagKey.of(RegistryKeys.ITEM, id("snail_food_items"));
	public static final TagKey<Biome> SNAIL_REGULAR_SPAWN_BIOMES = TagKey.of(RegistryKeys.BIOME, id("snail_spawn"));
	public static final TagKey<Biome> SNAIL_SWAMP_LIKE_SPAWN_BIOMES = TagKey.of(RegistryKeys.BIOME, id("swamp_like_spawn"));

	private static <T extends Item> T register(String name, T item) {
		return Registry.register(Registries.ITEM, id(name), item);
	}

	private static SoundEvent registerSound(String path) {
		var id = id(path);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void init() {}

	static {
		SNAIL_SPAWN_EGG_ITEM = register("snail_spawn_egg", new SnailSpawnEggItem(SNAIL_ENTITY_TYPE, 0xff36201c, 0xffd58d51,
				new FabricItemSettings()));
	}
}
