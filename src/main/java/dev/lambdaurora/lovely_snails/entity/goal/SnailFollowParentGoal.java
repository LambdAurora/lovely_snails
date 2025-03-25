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
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Modified {@link net.minecraft.world.entity.ai.goal.FollowParentGoal},
 * which uses a {@link SnailEntity#isBaby()} instead of {@link AgeableMob#getAge()}.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SnailFollowParentGoal extends Goal {
	private final SnailEntity self;
	private final double speed;
	private SnailEntity parent;
	private int delay;

	public SnailFollowParentGoal(SnailEntity self, double speed) {
		this.self = self;
		this.speed = speed;
	}

	@Override
	public boolean canUse() {
		if (this.self.getAge() >= 0) {
			return false;
		} else {
			var closeSnails = this.self.level().getEntitiesOfClass(SnailEntity.class,
					this.self.getBoundingBox().inflate(8.0, 4.0, 8.0)
			);
			SnailEntity closestParent = null;
			double closestParentDistance = Double.MAX_VALUE;

			for (var snail : closeSnails) {
				if (!snail.isBaby()) {
					double snailDistance = this.self.distanceToSqr(snail);
					if (!(snailDistance > closestParentDistance)) {
						closestParentDistance = snailDistance;
						closestParent = snail;
					}
				}
			}

			if (closestParent == null) {
				return false;
			} else if (closestParentDistance < 9.0) {
				return false;
			} else {
				this.parent = closestParent;
				return true;
			}
		}
	}

	@Override
	public boolean canContinueToUse() {
		if (!this.self.isBaby()) {
			return false;
		} else if (!this.parent.isAlive()) {
			return false;
		} else {
			double parentDistance = this.self.distanceToSqr(this.parent);
			return !(parentDistance < 9.0) && !(parentDistance > 256.0);
		}
	}

	@Override
	public void start() {
		this.delay = 0;
	}

	@Override
	public void stop() {
		this.parent = null;
	}

	@Override
	public void tick() {
		if (--this.delay <= 0) {
			this.delay = 10;
			this.self.getNavigation().moveTo(this.parent, this.speed);
		}
	}
}
