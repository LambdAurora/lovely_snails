/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AgeableMob.class)
public interface AgeableMobAccessor {
	@Accessor("DATA_BABY_ID")
	static EntityDataAccessor<Boolean> lovely_snails$getChild() {
		throw new UnsupportedOperationException("Mixin injection failed.");
	}
}
