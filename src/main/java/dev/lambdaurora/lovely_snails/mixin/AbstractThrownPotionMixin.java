/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractThrownPotion.class)
public abstract class AbstractThrownPotionMixin extends ThrowableItemProjectile {
	public AbstractThrownPotionMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(
			method = "onHitAsWater",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
			)
	)
	private void onWaterSplash(ServerLevel level, CallbackInfo ci, @Local AABB box) {
		var snails = level.getEntitiesOfClass(SnailEntity.class, box);
		for (var snail : snails) {
			snail.onWaterSplashed(this.getOwner());
		}
	}
}
