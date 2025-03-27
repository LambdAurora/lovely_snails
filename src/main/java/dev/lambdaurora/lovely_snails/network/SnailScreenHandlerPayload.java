/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SnailScreenHandlerPayload(int snailId, byte storagePage) {
	public static final StreamCodec<FriendlyByteBuf, SnailScreenHandlerPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SnailScreenHandlerPayload::snailId,
			ByteBufCodecs.BYTE, SnailScreenHandlerPayload::storagePage,
			SnailScreenHandlerPayload::new
	);
}
