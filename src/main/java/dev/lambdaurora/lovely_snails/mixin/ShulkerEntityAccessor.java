/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.mixin;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.mob.ShulkerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShulkerEntity.class)
public interface ShulkerEntityAccessor {
	@Accessor("COVERED_ARMOR_BONUS")
	static EntityAttributeModifier lovely_snails$getCoveredArmorBonus() {
		throw new UnsupportedOperationException("Mixin injection failed.");
	}
}
