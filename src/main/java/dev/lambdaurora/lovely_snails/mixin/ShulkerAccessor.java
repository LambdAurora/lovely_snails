/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.mixin;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Shulker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Shulker.class)
public interface ShulkerAccessor {
	@Accessor("COVERED_ARMOR_MODIFIER")
	static AttributeModifier lovely_snails$getCoveredArmorModifier() {
		throw new UnsupportedOperationException("Mixin injection failed.");
	}
}
