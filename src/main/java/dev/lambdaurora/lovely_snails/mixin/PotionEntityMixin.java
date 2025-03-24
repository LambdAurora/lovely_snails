/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.mixin;

import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin extends ThrownItemEntity {
	public PotionEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(
			method = "applyWaterEffects",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/World;getEntitiesByClass(Ljava/lang/Class;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onWaterSplash(CallbackInfo ci, Box box) {
		var snails = this.getWorld().getNonSpectatingEntities(SnailEntity.class, box);
		for (var snail : snails) {
			snail.onWaterSplashed(this.getOwner());
		}
	}
}
