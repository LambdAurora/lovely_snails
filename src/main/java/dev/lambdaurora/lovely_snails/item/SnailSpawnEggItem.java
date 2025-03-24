/*
 * Copyright © 2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;

/**
 * Represents a spawn egg that will try to sneak in where the spawn eggs are.
 *
 * @author LambdAurora
 * @version 1.1.1
 * @since 1.1.0
 */
public class SnailSpawnEggItem extends SpawnEggItem {
	public SnailSpawnEggItem(EntityType<? extends MobEntity> entityType, int primaryColor, int secondaryColor, Settings settings) {
		super(entityType, primaryColor, secondaryColor, settings);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
			entries.addItem(this);
		});
	}
}
