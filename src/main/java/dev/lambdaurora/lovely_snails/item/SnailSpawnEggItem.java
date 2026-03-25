/*
 * Copyright © 2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

/**
 * Represents a spawn egg that will try to sneak in where the spawn eggs are.
 *
 * @author LambdAurora
 * @version 1.3.0
 * @since 1.1.0
 */
public class SnailSpawnEggItem extends SpawnEggItem {
	public SnailSpawnEggItem(EntityType<? extends Mob> entityType, Item.Properties properties) {
		this(properties.spawnEgg(entityType));
	}

	public SnailSpawnEggItem(Item.Properties properties) {
		super(properties);

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
			entries.accept(this);
		});
	}
}
