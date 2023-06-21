/*
 * Copyright (c) 2022 LambdAurora <email@lambdaurora.dev>
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
