/*
 * Copyright (c) 2021 LambdAurora <aurora42lambda@gmail.com>
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
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.predicate.entity.EntityPredicates;

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
    }

    private boolean isThereScaryEntitiesAround() {
        var scaryEntities = this.snail.getEntityWorld().getOtherEntities(
                this.snail,
                this.snail.getBoundingBox().expand(this.vitalSpaceDistance, 3, this.vitalSpaceDistance),
                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(entity -> entity instanceof HostileEntity)
        );
        return !scaryEntities.isEmpty();
    }

    @Override
    public boolean canStart() {
        return this.isThereScaryEntitiesAround();
    }

    @Override
    public boolean shouldContinue() {
        return this.isThereScaryEntitiesAround();
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
