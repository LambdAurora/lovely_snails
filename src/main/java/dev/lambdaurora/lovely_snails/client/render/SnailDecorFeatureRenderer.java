/*
 * Copyright (c) 2021-2022 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.lovely_snails.client.render;

import dev.lambdaurora.lovely_snails.LovelySnails;
import dev.lambdaurora.lovely_snails.client.LovelySnailsClient;
import dev.lambdaurora.lovely_snails.client.model.SnailModel;
import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

/**
 * Renders decoration on a snail.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SnailDecorFeatureRenderer extends FeatureRenderer<SnailEntity, SnailModel> {
	private static final Identifier[] TEXTURES;
	private final SnailModel model;

	public SnailDecorFeatureRenderer(FeatureRendererContext<SnailEntity, SnailModel> featureRendererContext, EntityRendererFactory.Context context) {
		super(featureRendererContext);

		this.model = new SnailModel(context.getPart(LovelySnailsClient.SNAIL_DECOR_MODEL_LAYER));
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, SnailEntity entity,
	                   float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		var dyeColor = entity.getCarpetColor();
		if (dyeColor == null) return;
		var texture = TEXTURES[dyeColor.getId()];

		this.getContextModel().copyStateTo(this.model);
		this.model.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
		var vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
		this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.f, 1.f, 1.f, 1.f);
	}

	static {
		var colors = DyeColor.values();
		TEXTURES = new Identifier[colors.length];
		for (var color : colors) {
			TEXTURES[color.getId()] = LovelySnails.id("textures/entity/snail/decor/" + color.getName() + ".png");
		}
	}
}
