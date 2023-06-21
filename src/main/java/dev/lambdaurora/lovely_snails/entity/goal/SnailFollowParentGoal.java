/*
 * Copyright (c) 2021 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.lovely_snails.entity.goal;

import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import net.minecraft.entity.ai.goal.Goal;

/**
 * Modified {@link net.minecraft.entity.ai.goal.FollowParentGoal},
 * which uses a {@link SnailEntity#isBaby()} instead of {@link net.minecraft.entity.passive.AnimalEntity#getBreedingAge()}.
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
	public boolean canStart() {
		if (this.self.getBreedingAge() >= 0) {
			return false;
		} else {
			var closeSnails = this.self.world.getNonSpectatingEntities(SnailEntity.class,
					this.self.getBoundingBox().expand(8.0, 4.0, 8.0)
			);
			SnailEntity closestParent = null;
			double closestParentDistance = Double.MAX_VALUE;

			for (var snail : closeSnails) {
				if (!snail.isBaby()) {
					double snailDistance = this.self.squaredDistanceTo(snail);
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
	public boolean shouldContinue() {
		if (!this.self.isBaby()) {
			return false;
		} else if (!this.parent.isAlive()) {
			return false;
		} else {
			double parentDistance = this.self.squaredDistanceTo(this.parent);
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
			this.self.getNavigation().startMovingTo(this.parent, this.speed);
		}
	}
}
