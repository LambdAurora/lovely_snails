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

package dev.lambdaurora.lovely_snails.client.render;

import dev.lambdaurora.lovely_snails.LovelySnails;
import dev.lambdaurora.lovely_snails.client.LovelySnailsClient;
import dev.lambdaurora.lovely_snails.client.model.SnailModel;
import dev.lambdaurora.lovely_snails.entity.SnailEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.SaddleFeatureRenderer;
import net.minecraft.util.Identifier;

/**
 * Represents the snail entity renderer.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SnailEntityRenderer extends MobEntityRenderer<SnailEntity, SnailModel> {
    public static final Identifier TEXTURE = LovelySnails.id("textures/entity/snail/snail.png");

    public SnailEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new SnailModel(context.getPart(LovelySnailsClient.SNAIL_MODEL_LAYER)), .5f);

        this.addFeature(new SaddleFeatureRenderer<>(this,
                new SnailModel(context.getPart(LovelySnailsClient.SNAIL_SADDLE_MODEL_LAYER)),
                LovelySnails.id("textures/entity/snail/saddle.png")));
        this.addFeature(new SnailDecorFeatureRenderer(this, context));
    }

    @Override
    public Identifier getTexture(SnailEntity entity) {
        return TEXTURE;
    }
}
