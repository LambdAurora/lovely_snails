/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.entity.goal;

import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import dev.lambdaurora.lovely_snails.registry.LovelySnailsRegistry;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;

import java.util.EnumSet;

/**
 * Makes the snail hides if it senses danger nearby.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
public class SnailHideGoal extends Goal {
	private final SnailEntity snail;
	private final double vitalSpaceDistance;

	public SnailHideGoal(SnailEntity snail, double distance) {
		this.snail = snail;
		this.vitalSpaceDistance = distance;

		this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE, Flag.LOOK));
	}

	private boolean isThereScaryEntitiesAround() {
		var scaryEntities = this.snail.level().getEntities(
				this.snail,
				this.snail.getBoundingBox().inflate(this.vitalSpaceDistance, 3, this.vitalSpaceDistance),
				EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(entity -> entity instanceof Monster)
						.or(entity -> entity instanceof LivingEntity living
								&& living.getItemBySlot(EquipmentSlot.HEAD).is(LovelySnailsRegistry.SNAIL_SCARY_ITEMS)
						)
		);
		return !scaryEntities.isEmpty();
	}

	@Override
	public boolean canUse() {
		return this.snail.getLastHurtByMob() != null || this.isThereScaryEntitiesAround();
	}

	@Override
	public boolean canContinueToUse() {
		return this.snail.getLastHurtByMob() != null || this.isThereScaryEntitiesAround();
	}

	@Override
	public void start() {
		this.snail.getNavigation().stop();
		this.snail.setScared(true);
	}

	@Override
	public void stop() {
		this.snail.setScared(false);
	}
}
