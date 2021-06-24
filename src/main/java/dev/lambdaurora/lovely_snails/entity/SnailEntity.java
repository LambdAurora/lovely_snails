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

package dev.lambdaurora.lovely_snails.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.DyeColor;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the snail entity.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SnailEntity extends AnimalEntity implements Saddleable {
    private static final TrackedData<Byte> SNAIL_FLAGS = DataTracker.registerData(SnailEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Byte> CHEST_COUNT = DataTracker.registerData(SnailEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Integer> CARPET_COLOR = DataTracker.registerData(SnailEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final int TAMED_FLAG = 0b00000001;
    private static final int SADDLED_FLAG = 0b00000010;
    private static final int SCARED_FLAG = 0b00000100;

    private static final int SADDLE_SLOT = 0;
    private static final int CARPET_SLOT = 1;

    private Inventory inventory;

    public SnailEntity(EntityType<? extends SnailEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createSnailAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, .3f)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
    }

    protected boolean getSnailFlag(int bitmask) {
        return (this.dataTracker.get(SNAIL_FLAGS) & bitmask) != 0;
    }

    protected void setSnailFlag(int bitmask, boolean flag) {
        byte b = this.dataTracker.get(SNAIL_FLAGS);
        if (flag) {
            this.dataTracker.set(SNAIL_FLAGS, (byte) (b | bitmask));
        } else {
            this.dataTracker.set(SNAIL_FLAGS, (byte) (b & ~bitmask));
        }
    }

    public boolean isTame() {
        return this.getSnailFlag(TAMED_FLAG);
    }

    public void setCarpetColor(@Nullable DyeColor color) {
        this.dataTracker.set(CARPET_COLOR, color == null ? -1 : color.getId());
    }

    public @Nullable DyeColor getCarpetColor() {
        int i = this.dataTracker.get(CARPET_COLOR);
        return i == -1 ? null : DyeColor.byId(i);
    }

    /* Data Tracker Stuff */

    @Override
    protected void initDataTracker() {
        super.initDataTracker();

        this.dataTracker.startTracking(SNAIL_FLAGS, (byte) 0);
        this.dataTracker.startTracking(CHEST_COUNT, (byte) 0);
        this.dataTracker.startTracking(CARPET_COLOR, -1);
    }

    /* Inventory */

    public boolean hasChest() {
        return this.dataTracker.get(CHEST_COUNT) > 0;
    }

    public int getMaxChestCount() {
        return !this.isBaby() && this.isTame() ? 3 : 0;
    }

    protected int getInventorySize() {
        return 2 + this.getMaxChestCount() * 18;
    }

    public ItemStack getSaddle() {
        return this.inventory.getStack(SADDLE_SLOT);
    }

    /* Saddle Stuff */

    protected void updateSaddle() {
        if (!this.world.isClient()) {
            this.setSnailFlag(SADDLED_FLAG, !this.getSaddle().isEmpty());
        }
    }

    @Override
    public boolean canBeSaddled() {
        return this.isAlive() && !this.isBaby() && this.isTame();
    }

    @Override
    public void saddle(@Nullable SoundCategory sound) {

    }

    @Override
    public boolean isSaddled() {
        return this.getSnailFlag(SADDLED_FLAG);
    }

    /* Animal Stuff */

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }
}