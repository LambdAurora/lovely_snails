/*
 * Copyright © 2023 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.mixin;

import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import net.minecraft.entity.passive.PassiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PassiveEntity.class)
public class PassiveEntityMixin {
	@SuppressWarnings({"ConstantValue", "unchecked"})
	@ModifyArg(
			method = "initDataTracker",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/data/DataTracker;startTracking(Lnet/minecraft/entity/data/TrackedData;Ljava/lang/Object;)V"
			),
			index = 1
	)
	private <T> T lovely_snails$setDefaultBabyValue(T initialValue) {
		if (((Object) this) instanceof SnailEntity) {
			return (T) Boolean.TRUE;
		} else {
			return initialValue;
		}
	}
}
