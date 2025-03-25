/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.client.model;

import com.mojang.blaze3d.vertex.MatrixStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import static net.minecraft.client.model.geom.PartNames.*;

/**
 * Represents the snail entity model.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public class SnailModel extends EntityModel<SnailEntity> {
	public static final String SHELL = "shell";

	public static final float ADULT_SHELL_ROTATION = -0.0436f;
	private static final float ADULT_FRONT_WIDTH = 12.f;
	private static final float ADULT_SHELL_DIAMETER = 16.f;
	private static final float ADULT_EYE_YAW = 0.1745f;
	private static final float ADULT_EYE_LENGTH = 12.f;
	private static final float ADULT_EYE_DIAMETER = 2.f;

	public static final float BABY_SHELL_ROTATION = -0.080f;
	private static final float BABY_FRONT_WIDTH = 4.f;
	private static final float BABY_SHELL_DIAMETER = 10.f;
	private static final float BABY_EYE_YAW = ADULT_EYE_YAW;
	private static final float BABY_EYE_LENGTH = 7.f;
	private static final float BABY_EYE_DIAMETER = 1.f;

	private final Model adultModel;
	private final Model babyModel;

	public SnailModel(ModelPart root) {
		this.adultModel = new Model(root.getChild("adult"), ADULT_SHELL_ROTATION);
		this.babyModel = new Model(root.getChild("baby"), BABY_SHELL_ROTATION);
	}

	public static LayerDefinition model(CubeDeformation deformation) {
		var modelData = new MeshDefinition();
		var root = modelData.getRoot();
		buildAdultModel(root.addOrReplaceChild("adult", new ModelPartBuilder(), PartPose.ZERO), deformation);
		buildBabyModel(root.addOrReplaceChild("baby", new ModelPartBuilder(), PartPose.ZERO), deformation);
		return LayerDefinition.create(modelData, 128, 96);
	}

	private static void buildAdultModel(PartDefinition root, CubeDeformation deformation) {
		var body = root.addOrReplaceChild(BODY, new ModelPartBuilder()
						.uv(0, 32)
						.cuboid(-(ADULT_FRONT_WIDTH / 2.f), 5.f, -20.f, ADULT_FRONT_WIDTH, 3.f, 40.f, deformation),
				PartPose.offset(0.f, 16.f, -2.f));
		var upperBody = body.addOrReplaceChild("upper_body", new ModelPartBuilder()
						.uv(64, 16)
						.cuboid(-(ADULT_FRONT_WIDTH / 2.f), -7.f, 0.f, ADULT_FRONT_WIDTH, 12.f, 8.f,
								deformation),
				PartPose.offset(0.f, 0.f, -20.f));
		upperBody.addOrReplaceChild("left_tentacle", new ModelPartBuilder()
						.uv(0, 2)
						.cuboid(-ADULT_FRONT_WIDTH / 2.f, 0.f, -2.f, 4.f, 4.f, 2.f, deformation),
				PartPose.ZERO
		);
		upperBody.addOrReplaceChild("right_tentacle", new ModelPartBuilder()
						.uv(0, 2)
						.mirrored()
						.cuboid(ADULT_FRONT_WIDTH / 2.f - 4.f, 0.f, -2.f, 4.f, 4.f, 2.f, deformation),
				PartPose.ZERO
		);

		root.addOrReplaceChild(SHELL, new ModelPartBuilder()
						.cuboid(-(ADULT_FRONT_WIDTH / 2.f), 0.f, -2.f, ADULT_FRONT_WIDTH, ADULT_SHELL_DIAMETER, ADULT_SHELL_DIAMETER,
								deformation.extend(4.f, 8.f, 8.f),
								1.f, 1.f),
				PartPose.offsetAndRotation(0.f, -2.f, -5.f, ADULT_SHELL_ROTATION, 0.f, 0.f));

		body.addOrReplaceChild(LEFT_EYE, new ModelPartBuilder()
						.uv(42, 0)
						.cuboid(-2.8336f, -15.849f, -3.8272f, ADULT_EYE_DIAMETER, ADULT_EYE_LENGTH, ADULT_EYE_DIAMETER, deformation),
				PartPose.offsetAndRotation(-1.5f, -4.f, -15f, 0.4363f, ADULT_EYE_YAW, 0.f));
		body.addOrReplaceChild(RIGHT_EYE, new ModelPartBuilder()
						.uv(42, 0)
						.mirrored()
						.cuboid(0.8336f, -15.849f, -3.8272f, ADULT_EYE_DIAMETER, ADULT_EYE_LENGTH, ADULT_EYE_DIAMETER, deformation),
				PartPose.offsetAndRotation(1.5f, -4.f, -15f, 0.4363f, -ADULT_EYE_YAW, 0.f));
	}

	private static void buildBabyModel(PartDefinition babyRoot, CubeDeformation deformation) {
		var body = babyRoot.addOrReplaceChild(BODY, new ModelPartBuilder()
						.uv(56, 0)
						.cuboid(-(BABY_FRONT_WIDTH / 2.f), 22.f, -7.f, BABY_FRONT_WIDTH, 2.f, 14.f, deformation)
						.uv(0, 10)
						.cuboid(-(BABY_FRONT_WIDTH / 2.f), 20.f, -7.f, BABY_FRONT_WIDTH, 2.f, 4.f, deformation)
						.uv(0, 0)
						.cuboid(-(BABY_FRONT_WIDTH / 2.f), 22.f, -8.f, 1.f, 1.f, 1.f, deformation)
						.cuboid(BABY_FRONT_WIDTH / 2.f - 1.f, 22.f, -8.f, 1.f, 1.f, 1.f, deformation),
				PartPose.offset(0, 0, -2.f));
		babyRoot.addOrReplaceChild(SHELL, new ModelPartBuilder()
						.uv(0, 32)
						.cuboid(-3.f, 10.f, -1.f, 6.f, BABY_SHELL_DIAMETER, BABY_SHELL_DIAMETER, deformation),
				PartPose.offsetAndRotation(0.f, 2.2f, -3.f, BABY_SHELL_ROTATION, 0.f, 0.f));
		body.addOrReplaceChild(LEFT_EYE, new ModelPartBuilder()
						.uv(0, 32)
						.cuboid(-1.1664f, 19.f, -3.8272f, BABY_EYE_DIAMETER, BABY_EYE_LENGTH, BABY_EYE_DIAMETER, deformation),
				PartPose.offsetAndRotation(-1.5f, -4.f, -14.2f, 0.4363f, BABY_EYE_YAW, 0.f));
		body.addOrReplaceChild(RIGHT_EYE, new ModelPartBuilder()
						.uv(0, 32)
						.mirrored()
						.cuboid(0.1664f, 19.f, -3.8272f, BABY_EYE_DIAMETER, BABY_EYE_LENGTH, BABY_EYE_DIAMETER, deformation),
				PartPose.offsetAndRotation(1.5f, -4.f, -14.2f, 0.4363f, -BABY_EYE_YAW, 0.f));
	}

	public Model getCurrentModel() {
		return this.young ? this.babyModel : this.adultModel;
	}

	@Override
	public void setupAnim(SnailEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		var model = this.getCurrentModel();

		if (entity.isScared()) model.hideSnail();
		else model.uncover();
	}

	@Override
	public void renderToBuffer(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		matrices.push();
		this.getCurrentModel().render(matrices, vertices, light, overlay, red, green, blue, alpha);
		matrices.pop();
	}

	public static class Model {
		private final ModelPart root;
		private final ModelPart body;
		private final ModelPart shell;
		private final float idleShellYaw;

		public Model(ModelPart root, float idleShellYaw) {
			this.root = root;
			this.idleShellYaw = idleShellYaw;
			this.body = root.getChild(BODY);
			this.shell = root.getChild(SHELL);
		}

		/**
		 * Returns the shell of the snail.
		 *
		 * @return the shell
		 */
		public ModelPart getShell() {
			return this.shell;
		}

		/**
		 * Puts the snail in hiding.
		 */
		public void hideSnail() {
			this.body.visible = false;
			this.getShell().setRotation(0.f, 0.f, 0.f);
		}

		/**
		 * Puts the snail in idle position.
		 */
		public void uncover() {
			this.body.visible = true;
			this.getShell().setRotation(this.idleShellYaw, 0.f, 0.f);
		}

		public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
			if (!this.body.visible) matrices.translate(0, 2.f / 16.f, 0);
			this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
		}
	}
}
