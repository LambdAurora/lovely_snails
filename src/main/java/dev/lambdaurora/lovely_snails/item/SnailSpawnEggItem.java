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

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.collection.DefaultedList;

/**
 * Represents a spawn egg that will try to sneak in where the spawn eggs are.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.1.0
 */
public class SnailSpawnEggItem extends SpawnEggItem {
	public SnailSpawnEggItem(EntityType<? extends MobEntity> entityType, int primaryColor, int secondaryColor, Settings settings) {
		super(entityType, primaryColor, secondaryColor, settings);
	}

	@SuppressWarnings("deprecated")
	@Override
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
		if (this.isInGroup(group)) {
			String name = this.getBuiltInRegistryHolder().getRegistryKey().getValue().getPath();

			for (int i = 0; i < stacks.size(); i++) {
				var currentStack = stacks.get(i);

				if (currentStack.getItem() instanceof SpawnEggItem) {
					String otherName = currentStack.getItem().getBuiltInRegistryHolder().getRegistryKey().getValue().getPath();

					if (otherName.compareTo(name) > 0) {
						stacks.add(i, new ItemStack(this));
						return;
					}
				}
			}
		}
	}
}
