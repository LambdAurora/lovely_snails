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
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.predicate.entity.EntityPredicates;

import java.util.EnumSet;

/**
 * Makes the snail hides if it senses danger nearby.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SnailHideGoal extends Goal {
	private final SnailEntity snail;
	private final double vitalSpaceDistance;

	public SnailHideGoal(SnailEntity snail, double distance) {
		this.snail = snail;
		this.vitalSpaceDistance = distance;

		this.setControls(EnumSet.of(Control.JUMP, Control.MOVE, Control.LOOK));
	}

	private boolean isThereScaryEntitiesAround() {
		var scaryEntities = this.snail.getWorld().getOtherEntities(
				this.snail,
				this.snail.getBoundingBox().expand(this.vitalSpaceDistance, 3, this.vitalSpaceDistance),
				EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(entity -> entity instanceof HostileEntity)
		);
		return !scaryEntities.isEmpty();
	}

	@Override
	public boolean canStart() {
		return this.snail.getAttacker() != null || this.isThereScaryEntitiesAround();
	}

	@Override
	public boolean shouldContinue() {
		return this.snail.getAttacker() != null || this.isThereScaryEntitiesAround();
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
