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

package dev.lambdaurora.lovely_snails.registry;

import dev.lambdaurora.lovely_snails.LovelySnails;
import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.impl.object.builder.FabricEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.util.registry.Registry;

/**
 * Represents the Lovely Snails' registry.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class LovelySnailsRegistry {
    private LovelySnailsRegistry() {
        throw new UnsupportedOperationException("Someone tried to instantiate a class only containing static definitions. How?");
    }

    /* Entity */


    public static final EntityType<SnailEntity> SNAIL_ENTITY_TYPE = Registry.register(Registry.ENTITY_TYPE, LovelySnails.id("snail"),
            FabricEntityTypeBuilder.<SnailEntity>createMob()
                    .spawnGroup(SpawnGroup.CREATURE)
                    .entityFactory(SnailEntity::new)
                    .defaultAttributes(SnailEntity::createSnailAttributes)
                    .dimensions(EntityDimensions.changing(1.5f, 2.f))
                    .build()
    );

    public static void init() {
    }
}
