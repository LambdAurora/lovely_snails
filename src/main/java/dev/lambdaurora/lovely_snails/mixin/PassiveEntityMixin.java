/*
 * Copyright (c) 2023 LambdAurora <email@lambdaurora.dev>
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
