/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.entity;

import dev.lambdaurora.lovely_snails.LovelySnails;
import dev.lambdaurora.lovely_snails.SnailContainer;
import dev.lambdaurora.lovely_snails.entity.goal.SnailFollowParentGoal;
import dev.lambdaurora.lovely_snails.entity.goal.SnailHideGoal;
import dev.lambdaurora.lovely_snails.mixin.AgeableMobAccessor;
import dev.lambdaurora.lovely_snails.mixin.ShulkerAccessor;
import dev.lambdaurora.lovely_snails.network.SnailScreenHandlerPayload;
import dev.lambdaurora.lovely_snails.registry.LovelySnailsRegistry;
import dev.lambdaurora.lovely_snails.screen.SnailScreenHandler;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.function.Predicate;

/**
 * Represents the snail entity.
 *
 * @author LambdAurora
 * @version 1.2.1
 * @since 1.0.0
 */
public class SnailEntity extends TamableAnimal implements SnailContainer.Listener, HasCustomInventoryScreen {
	private static final AttributeModifier SCARED_ARMOR_BONUS = ShulkerAccessor.lovely_snails$getCoveredArmorModifier();

	private static final EntityDataAccessor<Boolean> CHILD = AgeableMobAccessor.lovely_snails$getChild();
	private static final EntityDataAccessor<Byte> SNAIL_FLAGS
			= SynchedEntityData.defineId(SnailEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> CHEST_FLAGS
			= SynchedEntityData.defineId(SnailEntity.class, EntityDataSerializers.BYTE);
	private static final int SCARED_FLAG = 0b0000_0001;
	private static final int INTERACTION_COOLDOWN_FLAG = 0b0000_0010;
	private static final int LOCKED_FLAG = 0b0000_0100;

	public static final int FIRST_CHEST_SLOT = 0;
	public static final int SECOND_CHEST_SLOT = 1;
	public static final int THIRD_CHEST_SLOT = 2;
	private static final int SATISFACTION_START = -256;

	private SnailContainer inventory;
	private int satisfaction;
	private short interactionCooldown;
	private boolean reading;

	public SnailEntity(EntityType<? extends SnailEntity> entityType, Level level) {
		super(entityType, level);
		this.updateInventory();
		//this.setMaxUpStep(1.f);
	}

	public static AttributeSupplier.Builder createSnailAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0)
				.add(Attributes.MOVEMENT_SPEED, .3f)
				.add(Attributes.ATTACK_DAMAGE, 2.0)
				.add(Attributes.FOLLOW_RANGE, 48.0);
	}

	public static boolean canSpawn(
			EntityType<? extends Animal> type,
			ServerLevelAccessor level,
			EntitySpawnReason spawnReason,
			BlockPos pos,
			RandomSource random
	) {
		var spawnBlock = level.getBlockState(pos.below());
		return level.getBrightness(LightLayer.SKY, pos) > 6 && spawnBlock.is(LovelySnailsRegistry.SNAIL_SPAWN_BLOCKS);
	}

	@Override
	public @NotNull SpawnGroupData finalizeSpawn(
			ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason,
			@Nullable SpawnGroupData entityData
	) {
		this.satisfaction = SATISFACTION_START + this.random.nextInt(10);
		this.setBaby(true);
		return super.finalizeSpawn(world, difficulty, spawnReason, entityData);
	}

	protected boolean getSnailFlag(int bitmask) {
		return (this.entityData.get(SNAIL_FLAGS) & bitmask) != 0;
	}

	protected void setSnailFlag(int bitmask, boolean flag) {
		byte b = this.entityData.get(SNAIL_FLAGS);
		if (flag) {
			this.entityData.set(SNAIL_FLAGS, (byte) (b | bitmask));
		} else {
			this.entityData.set(SNAIL_FLAGS, (byte) (b & ~bitmask));
		}
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

		if (!this.level().isClientSide()) {
			this.getAttribute(Attributes.ARMOR).removeModifier(SCARED_ARMOR_BONUS);
			if (scared) {
				this.getAttribute(Attributes.ARMOR).addPermanentModifier(SCARED_ARMOR_BONUS);
			}
		}
	}

	public int getSatisfaction() {
		if (this.level().isClientSide()) {
			return this.entityData.get(CHILD) ? -1 : 1;
		} else {
			return this.satisfaction;
		}
	}

	public void setSatisfaction(int satisfaction) {
		this.satisfaction = satisfaction;

		this.setBaby(this.shouldBeBaby());
	}

	/**
	 * Satisfies by the specified amount this snail.
	 *
	 * @param baseSatisfaction the base satisfaction amount
	 */
	public void satisfies(int baseSatisfaction) {
		Level level = this.level();

		if (this.isBaby()) {
			this.putInteractionOnCooldown();
			int newSatisfaction = this.getSatisfaction() + baseSatisfaction + this.random.nextInt(10);

			if (newSatisfaction >= 0) {
				var adultDimensions = this.getType().getDimensions();
				float width = adultDimensions.width() * .8f;
				float eyeHeight = adultDimensions.eyeHeight();
				var pos = BlockPos.containing(this.getX(), this.getY() + eyeHeight, this.getZ());
				var box = AABB.ofSize(new Vec3(this.getX(), this.getY() + eyeHeight, this.getZ()), width, 1.0E-6, width);

				// Adult form will suffocate, so we must prevent the growth until the player moves the snail.
				boolean willSuffocate = level.getBlockStates(box)
						.filter(Predicate.not(BlockBehaviour.BlockStateBase::isAir))
						.anyMatch(state -> state.isSuffocating(this.level(), pos));
				if (willSuffocate) {
					level.broadcastEntityEvent(this, (byte) 10);
					return;
				}
			}

			this.setSatisfaction(newSatisfaction);
		}

		level.broadcastEntityEvent(this, (byte) 8);
	}

	public short getInteractionCooldown() {
		if (this.level().isClientSide()) {
			return (short) (this.getSnailFlag(INTERACTION_COOLDOWN_FLAG) ? 1 : 0);
		} else {
			return this.interactionCooldown;
		}
	}

	public boolean canSatisfy() {
		return this.getInteractionCooldown() == 0;
	}

	public void setInteractionCooldown(int interactionCooldown) {
		boolean onCooldown = this.interactionCooldown > 0;
		if (onCooldown == (interactionCooldown == 0))
			this.setSnailFlag(INTERACTION_COOLDOWN_FLAG, interactionCooldown != 0);

		this.interactionCooldown = (short) interactionCooldown;
	}

	/**
	 * Puts interactions that brings satisfaction on cool-down.
	 */
	public void putInteractionOnCooldown() {
		this.setInteractionCooldown(75 + this.random.nextInt(10));
	}

	/**
	 * {@return {@code true} if this snail is locked, otherwise {@code false}}
	 */
	public boolean isLocked() {
		return this.getSnailFlag(LOCKED_FLAG);
	}

	/**
	 * Sets whether this is locked.
	 *
	 * @param locked {@code true} if this snail is locked, otherwise {@code false}
	 */
	public void setLocked(boolean locked) {
		this.setSnailFlag(LOCKED_FLAG, locked);
	}

	/**
	 * {@return {@code true} if the player is allowed to interact in any meaningful way with this snail, otherwise {@code false}}
	 *
	 * @param entity the entity that attempts to interact in any meaningful way with the snail
	 */
	public boolean canUseSnail(Entity entity) {
		return !this.isLocked() || (entity instanceof LivingEntity living && this.isOwnedBy(living));
	}

	public static @Nullable DyeColor getColorFromCarpet(ItemStack color) {
		var block = Block.byItem(color.getItem());
		return block instanceof WoolCarpetBlock dyedCarpetBlock ? dyedCarpetBlock.getColor() : null;
	}

	public @Nullable DyeColor getCarpetColor() {
		return getColorFromCarpet(this.equipment.get(EquipmentSlot.BODY));
	}

	@Override
	public void handleEntityEvent(byte event) {
		if (event == 8) {
			for (int i = 0; i < 7; ++i) {
				double xOffset = this.random.nextGaussian() * 0.02;
				double yOffset = this.random.nextGaussian() * 0.02;
				double zOffset = this.random.nextGaussian() * 0.02;
				this.level().addParticle(this.random.nextBoolean() ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.HEART,
						this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0),
						xOffset, yOffset, zOffset);
			}
		} else if (event == 9) {
			for (int i = 0; i < 7; ++i) {
				double xOffset = this.random.nextGaussian() * 0.02;
				double yOffset = this.random.nextGaussian() * 0.02;
				double zOffset = this.random.nextGaussian() * 0.02;
				this.level().addParticle(ParticleTypes.ANGRY_VILLAGER,
						this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0),
						xOffset, yOffset, zOffset);
			}
		} else if (event == 10) {
			for (int i = 0; i < 7; ++i) {
				double xOffset = this.random.nextGaussian() * 0.02;
				double yOffset = this.random.nextGaussian() * 0.02;
				double zOffset = this.random.nextGaussian() * 0.02;
				this.level().addParticle(this.random.nextBoolean() ? ParticleTypes.ANGRY_VILLAGER : ParticleTypes.SMOKE,
						this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0),
						xOffset, yOffset, zOffset);
			}
		} else
			super.handleEntityEvent(event);
	}

	@Override
	public boolean requiresCustomPersistence() {
		return super.requiresCustomPersistence() || this.isTame();
	}

	/* Data Tracker Stuff */

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(SNAIL_FLAGS, (byte) 0);
		builder.define(CHEST_FLAGS, (byte) 0);
	}

	/* Serialization */

	@Override
	public void readAdditionalSaveData(ValueInput input) {
		this.reading = true;
		super.readAdditionalSaveData(input);

		this.setSatisfaction(input.getIntOr("satisfaction", SATISFACTION_START));
		this.setInteractionCooldown(input.getShortOr("interaction_cooldown", (short) 0));
		this.setLocked(input.getBooleanOr("locked", false));

		input.read("saddle", ItemStack.CODEC).ifPresent(stack -> this.equipment.set(EquipmentSlot.SADDLE, stack));
		input.read("decor", ItemStack.CODEC).ifPresent(stack -> this.equipment.set(EquipmentSlot.BODY, stack));

		LovelySnails.readInventory(input, "chests", this.inventory, 0);
		LovelySnails.readInventory(input, "inventory", this.inventory, 3);

		this.syncInventoryToFlags();
		this.reading = false;
	}

	@Override
	public void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.discard("Sitting"); // We don't actually need that as you can't make a snail sit.

		output.putInt("satisfaction", this.getSatisfaction());
		output.putShort("interaction_cooldown", this.getInteractionCooldown());
		output.putBoolean("locked", this.isLocked());

		LovelySnails.writeInventory(output, "chests", this.inventory, 0, 3);
		LovelySnails.writeInventory(output, "inventory", this.inventory, 3, this.inventory.getContainerSize());
	}

	/* AI */

	@Override
	protected void registerGoals() {
		super.registerGoals();

		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.2));
		this.goalSelector.addGoal(1, new SnailHideGoal(this, 5));
		this.goalSelector.addGoal(2, new BreedGoal(this, 1.0, SnailEntity.class));
		this.goalSelector.addGoal(4, new SnailFollowParentGoal(this, 1.0));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.f));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	/* Inventory */

	public int getChestFlags() {
		return this.entityData.get(CHEST_FLAGS);
	}

	public ItemStack getChest(int slot) {
		int flags = this.getChestFlags();

		int chest = (flags >> slot * 2) & 3;

		return switch (chest) {
			case 1 -> new ItemStack(Items.CHEST);
			case 2 -> new ItemStack(Items.ENDER_CHEST);
			default -> ItemStack.EMPTY;
		};
	}

	public int getInventorySize() {
		return 48;
	}

	public ItemStack getSaddle() {
		return this.equipment.get(EquipmentSlot.SADDLE);
	}

	/**
	 * Syncs the flags with the inventory.
	 */
	public void syncInventoryToFlags() {
		if (!this.level().isClientSide()) {
			int chestFlags = 0;
			for (int chest = 0; chest < 3; chest++) {
				var chestStack = this.inventory.getItem(chest);
				int flag = 0;

				if (chestStack.is(Items.CHEST))
					flag = 1;
				else if (chestStack.is(Items.ENDER_CHEST))
					flag = 2;

				chestFlags |= flag << chest * 2;
			}
			this.entityData.set(CHEST_FLAGS, (byte) chestFlags);
		}
	}
	
	@Override
	public void openCustomInventoryScreen(Player player) {
		if (!this.level().isClientSide() && (!this.isVehicle() || this.hasPassenger(player)) && this.isTame()) {
			player.openMenu(new SnailScreenHandlerFactory());
		}
	}

	public void openEnderChestInventory(Player player) {
		if (!this.level().isClientSide() && (!this.isVehicle() || this.hasPassenger(player)) && this.isTame()) {
			player.openMenu(new SimpleMenuProvider((syncId, playerInventory, playerEntity) -> {
				return ChestMenu.threeRows(syncId, playerInventory, player.getEnderChestInventory());
			}, Component.translatable("container.enderchest")));
		}
	}

	public boolean isInventoryDifferent(Container inventory) {
		return this.inventory != inventory;
	}

	@Override
	protected void dropEquipment(ServerLevel level) {
		super.dropEquipment(level);

		if (this.inventory != null) {
			for (int slot = 0; slot < this.inventory.getContainerSize(); ++slot) {
				var stack = this.inventory.getItem(slot);
				if (!stack.isEmpty() && !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP))
					this.spawnAtLocation(level, stack);
			}
		}
	}

	protected void updateInventory() {
		var previousInventory = this.inventory;
		this.inventory = new SnailContainer(this.getInventorySize());
		if (previousInventory != null) {
			previousInventory.removeListener(this);
			int maxSize = Math.min(previousInventory.getContainerSize(), this.inventory.getContainerSize());

			for (int slot = 0; slot < maxSize; ++slot) {
				var stack = previousInventory.getItem(slot);
				if (!stack.isEmpty()) {
					this.inventory.setItem(slot, stack.copy());
				}
			}
		}

		this.inventory.addListener(this);
		this.syncInventoryToFlags();
	}

	@Override
	public void containerChanged(Container sender) {
		boolean previouslySaddled = this.isSaddled();
		boolean hadDecor = this.getCarpetColor() != null;
		this.syncInventoryToFlags();
		if (this.age > 20 && !previouslySaddled && this.isSaddled()) {
			this.playSound(SoundEvents.HORSE_SADDLE.value(), .5f, 1.f);
		}

		if (!this.reading && !this.level().isClientSide() && !hadDecor && this.getCarpetColor() != null && this.canSatisfy()) {
			var biome = this.level().getBiome(this.blockPosition());

			int baseSatisfaction;
			if (biome.value().warmEnoughToRain(this.blockPosition(), this.level().getSeaLevel())) baseSatisfaction = 15;
			else baseSatisfaction = 5;
			this.satisfies(baseSatisfaction);
		}
	}

	/* Interaction */

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		Level level = this.level();
		var handStack = player.getItemInHand(hand);

		if (this.isTame() && player.isSecondaryUseActive()) {
			this.openCustomInventoryScreen(player);
			return InteractionResult.SUCCESS;
		}

		if (this.isVehicle()) {
			return super.mobInteract(player, hand);
		}

		if (!handStack.isEmpty()) {
			var itemResult = handStack.interactLivingEntity(player, this, hand);
			if (itemResult.consumesAction()) {
				return itemResult;
			}

			if (this.isFood(handStack) && !this.isScared()) {
				if (this.isTame() && this.canUseSnail(player)) {
					int age = this.getAge();

					if (!level.isClientSide() && age == 0 && this.canFallInLove()) {
						this.usePlayerItem(player, hand, handStack);
						this.setInLove(player);
						this.gameEvent(GameEvent.EAT);
						return InteractionResult.SUCCESS;
					} else if (level.isClientSide()) {
						return InteractionResult.CONSUME;
					}
				} else {
					this.usePlayerItem(player, hand, handStack);

					if (!this.isLocked() && this.random.nextInt(3) == 0) {
						this.tame(player);
						level.broadcastEntityEvent(this, (byte) 7);
					} else {
						level.broadcastEntityEvent(this, (byte) 6);
					}

					return InteractionResult.SUCCESS;
				}
			}

			if (!this.isTame()) {
				return InteractionResult.CONSUME;
			}

			boolean saddle = !this.isBaby() && !this.isSaddled() && handStack.is(Items.SADDLE);
			if (getColorFromCarpet(handStack) != null || saddle) {
				this.openCustomInventoryScreen(player);
				return InteractionResult.SUCCESS;
			}
		}

		if (this.isTame()) {
			if (!this.isBaby()) {
				if (!level.isClientSide()) {
					player.setYRot(this.getYRot());
					player.setXRot(this.getXRot());
					player.startRiding(this);
				}

				return InteractionResult.SUCCESS;
			} else if (this.canSatisfy() && this.getOwner() == player) {
				boolean likeItem = handStack.is(LovelySnailsRegistry.SNAIL_FOOD_ITEMS);
				if (handStack.isEmpty() || likeItem) {
					if (likeItem) this.usePlayerItem(player, hand, handStack);
					// What about petting a snail?
					if (!level.isClientSide())
						this.satisfies(likeItem ? 20 : 10);

					return InteractionResult.SUCCESS;
				} else if (handStack.is(Items.POISONOUS_POTATO)) {
					// Watch me break one of Jeb's rule.
					// Also why the fuck would you give a poisonous potato to a snail?
					if (!level.isClientSide()) {
						this.usePlayerItem(player, hand, handStack);
						this.setSatisfaction(this.getSatisfaction() - 4000);
						this.putInteractionOnCooldown();

						level.broadcastEntityEvent(this, (byte) 9);
					}

					return InteractionResult.SUCCESS;
				}
			}
		}

		return super.mobInteract(player, hand);
	}

	public void onWaterSplashed(Entity waterOwner) {
		if (this.getOwner() != waterOwner)
			return;

		if (this.canSatisfy()) {
			var biome = this.level().getBiome(this.blockPosition());

			int baseSatisfaction;
			if (!biome.value().hasPrecipitation()) baseSatisfaction = 20;
			else if (biome.value().warmEnoughToRain(this.blockPosition(), this.level().getSeaLevel())) baseSatisfaction = 10;
			else baseSatisfaction = 15;
			this.satisfies(baseSatisfaction);
		}
	}

	/* Saddle Stuff */

	@Override
	public boolean canUseSlot(EquipmentSlot equipmentSlot) {
		return switch (equipmentSlot) {
			case SADDLE -> this.isAlive() && !this.isBaby() && this.isTame();
			case BODY -> this.isAlive() && this.isTame();
			default -> false;
		};
	}

	@Override
	public boolean isSaddled() {
		return !this.getItemBySlot(EquipmentSlot.SADDLE).isEmpty();
	}

	/* Leashing */

	@Override
	public boolean supportQuadLeash() {
		return !this.isBaby();
	}

	@Override
	public Vec3 @NotNull [] getQuadLeashOffsets() {
		return Leashable.createQuadLeashOffsets(this, -0.06, 0.64, 0.38, 1);
	}

	/* Riding */

	@Override
	public @Nullable LivingEntity getControllingPassenger() {
		if (this.isLocked()) return null;
		if (!this.isSaddled()) return null;

		Entity passenger = this.getFirstPassenger();

		if (passenger instanceof LivingEntity livingPassenger) {
			return livingPassenger;
		} else {
			return null;
		}
	}

	private @Nullable Vec3 tryDismountTowards(Vec3 vec3d, LivingEntity livingEntity) {
		double targetX = this.getX() + vec3d.x;
		double targetY = this.getBoundingBox().minY;
		double targetZ = this.getZ() + vec3d.z;
		var pos = new BlockPos.MutableBlockPos();

		for (var pose : livingEntity.getDismountPoses()) {
			pos.set(targetX, targetY, targetZ);
			double maxDismountY = this.getBoundingBox().maxY + 0.75;

			while (true) {
				double dismountHeight = this.level().getBlockFloorHeight(pos);
				if (pos.getY() + dismountHeight > maxDismountY) {
					break;
				}

				if (DismountHelper.isBlockFloorValid(dismountHeight)) {
					var poseBoundingBox = livingEntity.getLocalBoundsForPose(pose);
					var dismountPos = new Vec3(targetX, pos.getY() + dismountHeight, targetZ);
					if (DismountHelper.canDismountTo(this.level(), livingEntity, poseBoundingBox.move(dismountPos))) {
						livingEntity.setPose(pose);
						return dismountPos;
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
	public @NotNull Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
		var rightDismountOffset = getCollisionHorizontalEscapeVector(this.getBbWidth(), passenger.getBbWidth(),
				this.getYRot() + (passenger.getMainArm() == HumanoidArm.RIGHT ? 90.f : -90.f));
		var dismountPos = this.tryDismountTowards(rightDismountOffset, passenger);

		if (dismountPos != null) {
			return dismountPos;
		} else {
			var leftDismountOffset = getCollisionHorizontalEscapeVector(this.getBbWidth(), passenger.getBbWidth(),
					this.getYRot() + (passenger.getMainArm() == HumanoidArm.LEFT ? 90.f : -90.f));
			dismountPos = this.tryDismountTowards(leftDismountOffset, passenger);
			return dismountPos != null ? dismountPos : this.position();
		}
	}

	/* Movement */

	@Override
	public void aiStep() {
		super.aiStep();

		if (!this.level().isClientSide() && this.isAlive()) {
			if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
				this.heal(1.f);
			}

			short interactionCooldown = this.getInteractionCooldown();
			if (interactionCooldown != 0) {
				this.setInteractionCooldown(interactionCooldown - 1);
			}
		}
	}

	@Override
	public void travel(Vec3 movementInput) {
		if (this.isAlive()) {
			Entity primaryPassenger = this.getControllingPassenger();

			if (primaryPassenger != null && this.isSaddled() && this.canUseSnail(primaryPassenger)) {
				if (this.isScared()) { // When the snail is scared, the snail is paralyzed.
					return;
				}

				var rider = (LivingEntity) primaryPassenger;
				//noinspection ConstantConditions
				this.setYRot(rider.getYRot());
				this.yRotO = this.getYRot();
				this.setXRot(rider.getXRot() * .5f);
				this.setRot(this.getYRot(), this.getXRot());
				this.yBodyRot = this.getYRot();
				this.yHeadRot = this.yBodyRot;
				float sidewaysSpeed = rider.xxa * .25f;
				float forwardSpeed = rider.zza * .4f;
				if (forwardSpeed <= 0.f) {
					forwardSpeed *= .25f;
				}

				if (this.isLocalClientAuthoritative()) {
					this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
					super.travel(new Vec3(sidewaysSpeed, movementInput.y, forwardSpeed));
				} else if (rider instanceof Player) {
					this.setDeltaMovement(Vec3.ZERO);
				}

				this.calculateEntityAnimation(false);
			} else {
				super.travel(movementInput);
			}
		}
	}

	@Override
	public boolean isPushable() {
		return !this.isVehicle();
	}

	@Override
	protected boolean isImmobile() {
		return super.isImmobile() || (this.isVehicle() && this.isSaddled() && this.canUseSnail(this.getControllingPassenger()));
	}

	/* Sounds */

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return LovelySnailsRegistry.SNAIL_HURT_SOUND_EVENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return LovelySnailsRegistry.SNAIL_DEATH_SOUND_EVENT;
	}

	/* Passive Stuff */

	@Override
	public void setAge(int age) {
		this.age = age;
	}



	@Override
	protected void ageBoundaryReached() {
		if (this.level() instanceof ServerLevel level && !this.isBaby() && level.getGameRules().get(GameRules.MOB_DROPS)) {
			this.spawnAtLocation(level, new ItemStack(Items.SLIME_BALL, 1 + this.random.nextInt(2)));
		}
	}

	protected boolean shouldBeBaby() {
		return this.satisfaction < 0;
	}

	@Override
	public boolean isBaby() {
		return this.entityData.get(CHILD);
	}

	@Override
	public void setBaby(boolean baby) {
		var wasBaby = this.entityData.get(CHILD);
		this.entityData.set(CHILD, baby);

		if (wasBaby && !baby && !this.reading) {
			this.ageBoundaryReached();
		}
	}

	/* Animal Stuff */

	@Override
	public SnailEntity getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
		var child = LovelySnailsRegistry.SNAIL_ENTITY_TYPE.create(level, EntitySpawnReason.BREEDING);

		if (otherParent instanceof SnailEntity) {
			if (this.isTame()) {
				child.setOwnerReference(this.getOwnerReference());
				child.setTame(true, false);
			}
		}

		return child;
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return stack.is(LovelySnailsRegistry.SNAIL_BREEDING_ITEMS);
	}

	@Override
	public float getAgeScale() {
		return this.isBaby() ? 0.35f : 1.f;
	}

	@NullMarked
	private class SnailScreenHandlerFactory implements ExtendedMenuProvider<SnailScreenHandlerPayload> {
		private SnailEntity snail() {
			return SnailEntity.this;
		}

		@Override
		public Component getDisplayName() {
			return this.snail().getDisplayName();
		}

		@Override
		public SnailScreenHandlerPayload getScreenOpeningData(ServerPlayer player) {
			return new SnailScreenHandlerPayload(
					this.snail().getId(),
					(byte) SnailScreenHandler.getOpeningStoragePage(this.snail().inventory)
			);
		}

		@Override
		public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
			var snailInv = this.snail().inventory;
			return new SnailScreenHandler(syncId, inv, snailInv, this.snail(), SnailScreenHandler.getOpeningStoragePage(snailInv));
		}
	}
}
