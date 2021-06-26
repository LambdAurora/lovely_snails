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

import dev.lambdaurora.lovely_snails.entity.goal.SnailHideGoal;
import dev.lambdaurora.lovely_snails.screen.SnailScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.DyedCarpetBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Represents the snail entity.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SnailEntity extends AnimalEntity implements InventoryChangedListener, Saddleable {
    private static final TrackedData<Byte> SNAIL_FLAGS = DataTracker.registerData(SnailEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Byte> CHEST_COUNT = DataTracker.registerData(SnailEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Integer> CARPET_COLOR = DataTracker.registerData(SnailEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final int TAMED_FLAG = 0b00000001;
    private static final int SADDLED_FLAG = 0b00000010;
    private static final int SCARED_FLAG = 0b00000100;

    public static final int SADDLE_SLOT = 0;
    public static final int CARPET_SLOT = 1;

    private SimpleInventory inventory;

    public SnailEntity(EntityType<? extends SnailEntity> entityType, World world) {
        super(entityType, world);
        this.updateInventory();
        this.stepHeight = 1.f;
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

    /**
     * Returns whether this snail is scared of something.
     *
     * @return {@code true} if this snail is scared, else {@code false}
     */
    public boolean isScared() {
        return this.getSnailFlag(SCARED_FLAG);
    }

    /**
     * Sets whether this snail is scared of something.
     *
     * @param scared {@code true} if this snail is scared, else {@code false}
     */
    public void setScared(boolean scared) {
        this.setSnailFlag(SCARED_FLAG, scared);
    }

    public static @Nullable DyeColor getColorFromCarpet(ItemStack color) {
        var block = Block.getBlockFromItem(color.getItem());
        return block instanceof DyedCarpetBlock dyedCarpetBlock ? dyedCarpetBlock.getDyeColor() : null;
    }

    public void setCarpetColor(@Nullable DyeColor color) {
        this.dataTracker.set(CARPET_COLOR, color == null ? -1 : color.getId());
    }

    public @Nullable DyeColor getCarpetColor() {
        int i = this.dataTracker.get(CARPET_COLOR);
        return i == -1 ? null : DyeColor.byId(i);
    }

    @Override
    public double getMountedHeightOffset() {
        if (!this.isBaby())
            return this.getDimensions(EntityPose.STANDING).height * 0.95f;
        else
            return super.getMountedHeightOffset();
    }

    /* Data Tracker Stuff */

    @Override
    protected void initDataTracker() {
        super.initDataTracker();

        this.dataTracker.startTracking(SNAIL_FLAGS, (byte) 0);
        this.dataTracker.startTracking(CHEST_COUNT, (byte) 0);
        this.dataTracker.startTracking(CARPET_COLOR, -1);
    }

    /* Serialization */

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        this.setSnailFlag(TAMED_FLAG, nbt.getBoolean("tame"));

        this.readSpecialSlot(nbt, "saddle", SADDLE_SLOT, stack -> stack.isOf(Items.SADDLE));
        this.readSpecialSlot(nbt, "decor", CARPET_SLOT,
                stack -> stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CarpetBlock
        );

        this.syncInventoryToFlags();
    }

    private void readSpecialSlot(NbtCompound nbt, String name, int slot, Predicate<ItemStack> predicate) {
        if (nbt.contains(name, NbtElement.COMPOUND_TYPE)) {
            var stack = ItemStack.fromNbt(nbt.getCompound(name));
            if (predicate.test(stack)) {
                this.inventory.setStack(slot, stack);
                return;
            }
        }

        this.inventory.setStack(slot, ItemStack.EMPTY);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        nbt.putBoolean("tame", this.isTame());

        this.writeSpecialSlot(nbt, "saddle", SADDLE_SLOT);
        this.writeSpecialSlot(nbt, "decor", CARPET_SLOT);
    }

    public void writeSpecialSlot(NbtCompound nbt, String name, int slot) {
        if (!this.inventory.getStack(slot).isEmpty()) {
            nbt.put(name, this.inventory.getStack(slot).writeNbt(new NbtCompound()));
        }
    }

    /* AI */

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.2));
        this.goalSelector.add(1, new SnailHideGoal(this, 5));
        this.goalSelector.add(2, new AnimalMateGoal(this, 1.0, SnailEntity.class));
        this.goalSelector.add(4, new FollowParentGoal(this, 1.0));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.f));
        this.goalSelector.add(8, new LookAroundGoal(this));
    }

    /* Inventory */

    public int getChests() {
        return this.dataTracker.get(CHEST_COUNT);
    }

    public boolean hasChest() {
        return this.getChests() > 0;
    }

    public int getMaxChestCount() {
        return !this.isBaby() && this.isTame() ? 3 : 0;
    }

    public int getInventorySize() {
        return 2 + this.getChests() * 18;
    }

    public ItemStack getSaddle() {
        return this.inventory.getStack(SADDLE_SLOT);
    }

    public void syncInventoryToFlags() {
        if (!this.world.isClient()) {
            this.setSnailFlag(SADDLED_FLAG, !this.getSaddle().isEmpty());
            this.setCarpetColor(getColorFromCarpet(this.inventory.getStack(CARPET_SLOT)));
        }
    }

    public void openInventory(PlayerEntity player) {
        if (!this.world.isClient() && (!this.hasPassengers() || this.hasPassenger(player)) && this.isTame()) {
            player.openHandledScreen(new SnailScreenHandlerFactory());
        }
    }

    public boolean isInventoryDifferent(Inventory inventory) {
        return this.inventory != inventory;
    }

    protected void updateInventory() {
        var previousInventory = this.inventory;
        this.inventory = new SimpleInventory(this.getInventorySize());
        if (previousInventory != null) {
            previousInventory.removeListener(this);
            int maxSize = Math.min(previousInventory.size(), this.inventory.size());

            for (int slot = 0; slot < maxSize; ++slot) {
                var stack = previousInventory.getStack(slot);
                if (!stack.isEmpty()) {
                    this.inventory.setStack(slot, stack.copy());
                }
            }
        }

        this.inventory.addListener(this);
        this.syncInventoryToFlags();
    }

    @Override
    public void onInventoryChanged(Inventory sender) {
        boolean previouslySaddled = this.isSaddled();
        this.syncInventoryToFlags();
        if (this.age > 20 && !previouslySaddled && this.isSaddled()) {
            this.playSound(SoundEvents.ENTITY_HORSE_SADDLE, .5f, 1.f);
        }
    }

    /* Interaction */

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        var handStack = player.getStackInHand(hand);

        if (this.isTame() && player.shouldCancelInteraction()) {
            this.openInventory(player);
            return ActionResult.success(this.world.isClient());
        }

        if (this.hasPassengers()) {
            return super.interactMob(player, hand);
        }

        if (!handStack.isEmpty()) {
            var itemResult = handStack.useOnEntity(player, this, hand);
            if (itemResult.isAccepted()) {
                return itemResult;
            }

            if (!this.isTame()) {
                return ActionResult.success(this.world.isClient());
            }

            boolean saddle = !this.isBaby() && !this.isSaddled() && handStack.isOf(Items.SADDLE);
            if (getColorFromCarpet(handStack) != null || saddle) {
                this.openInventory(player);
                return ActionResult.success(this.world.isClient());
            }
        }

        if (!this.isBaby() && this.isTame()) {
            if (!this.getEntityWorld().isClient()) {
                player.setYaw(this.getYaw());
                player.setPitch(this.getPitch());
                player.startRiding(this);
            }

            return ActionResult.success(this.getEntityWorld().isClient());
        }

        return super.interactMob(player, hand);
    }

    /* Saddle Stuff */

    @Override
    public boolean canBeSaddled() {
        return this.isAlive() && !this.isBaby() && this.isTame();
    }

    @Override
    public void saddle(@Nullable SoundCategory sound) {
        this.inventory.setStack(0, new ItemStack(Items.SADDLE));
        if (sound != null) {
            this.world.playSoundFromEntity(null, this, SoundEvents.ENTITY_HORSE_SADDLE, sound, 0.5F, 1.0F);
        }
    }

    @Override
    public boolean isSaddled() {
        return this.getSnailFlag(SADDLED_FLAG);
    }

    /* Riding */

    @Override
    public @Nullable Entity getPrimaryPassenger() {
        return this.getFirstPassenger();
    }

    @Override
    public boolean canBeControlledByRider() {
        return this.getPrimaryPassenger() instanceof LivingEntity;
    }

    private @Nullable Vec3d tryDismountTowards(Vec3d vec3d, LivingEntity livingEntity) {
        double targetX = this.getX() + vec3d.x;
        double targetY = this.getBoundingBox().minY;
        double targetZ = this.getZ() + vec3d.z;
        var pos = new BlockPos.Mutable();

        for (var pose : livingEntity.getPoses()) {
            pos.set(targetX, targetY, targetZ);
            double maxDismountY = this.getBoundingBox().maxY + 0.75;

            while (true) {
                double dismountHeight = this.world.getDismountHeight(pos);
                if (pos.getY() + dismountHeight > maxDismountY) {
                    break;
                }

                if (Dismounting.canDismountInBlock(dismountHeight)) {
                    var poseBoundingBox = livingEntity.getBoundingBox(pose);
                    var vec3d2 = new Vec3d(targetX, pos.getY() + dismountHeight, targetZ);
                    if (Dismounting.canPlaceEntityAt(this.world, livingEntity, poseBoundingBox.offset(vec3d2))) {
                        livingEntity.setPose(pose);
                        return vec3d2;
                    }
                }

                pos.move(Direction.UP);
                if (!(pos.getY() < maxDismountY)) {
                    break;
                }
            }
        }

        return null;
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        var rightDismountOffset = getPassengerDismountOffset(this.getWidth(), passenger.getWidth(),
                this.getYaw() + (passenger.getMainArm() == Arm.RIGHT ? 90.f : -90.f));
        var dismountPos = this.tryDismountTowards(rightDismountOffset, passenger);
        if (dismountPos != null) {
            return dismountPos;
        } else {
            var leftDismountOffset = getPassengerDismountOffset(this.getWidth(), passenger.getWidth(),
                    this.getYaw() + (passenger.getMainArm() == Arm.LEFT ? 90.f : -90.f));
            dismountPos = this.tryDismountTowards(leftDismountOffset, passenger);
            return dismountPos != null ? dismountPos : this.getPos();
        }
    }

    /* Movement */

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.world.isClient() && this.isAlive()) {
            if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
                this.heal(1.f);
            }
        }
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.isAlive()) {
            if (this.hasPassengers() && this.canBeControlledByRider() && this.isSaddled()) {
                if (this.isScared()) { // When the snail is scared, the snail is paralyzed.
                    this.checkBlockCollision();
                    return;
                }

                var primaryPassenger = (LivingEntity) this.getPrimaryPassenger();
                //noinspection ConstantConditions
                this.setYaw(primaryPassenger.getYaw());
                this.prevYaw = this.getYaw();
                this.setPitch(primaryPassenger.getPitch() * .5f);
                this.setRotation(this.getYaw(), this.getPitch());
                this.bodyYaw = this.getYaw();
                this.headYaw = this.bodyYaw;
                float sidewaysSpeed = primaryPassenger.sidewaysSpeed * .25f;
                float forwardSpeed = primaryPassenger.forwardSpeed * .4f;
                if (forwardSpeed <= 0.f) {
                    forwardSpeed *= .25f;
                }

                this.flyingSpeed = this.getMovementSpeed() * .1f;
                if (this.isLogicalSideForUpdatingMovement()) {
                    this.setMovementSpeed((float) this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
                    super.travel(new Vec3d(sidewaysSpeed, movementInput.y, forwardSpeed));
                } else if (primaryPassenger instanceof PlayerEntity) {
                    this.setVelocity(Vec3d.ZERO);
                }

                this.updateLimbs(this, false);
                this.tryCheckBlockCollision();
            } else {
                this.flyingSpeed = .02f;
                super.travel(movementInput);
            }
        }
    }

    @Override
    public boolean isPushable() {
        return !this.hasPassengers();
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() && this.hasPassengers() && this.isSaddled();
    }

    /* Animal Stuff */

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public float getScaleFactor() {
        return this.isBaby() ? 0.35f : 1.f;
    }

    private class SnailScreenHandlerFactory implements ExtendedScreenHandlerFactory {
        private SnailEntity snail() {
            return SnailEntity.this;
        }

        @Override
        public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
            buf.writeVarInt(this.snail().getId());
        }

        @Override
        public Text getDisplayName() {
            return this.snail().getDisplayName();
        }

        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
            return new SnailScreenHandler(syncId, inv, this.snail().inventory, this.snail());
        }
    }
}
