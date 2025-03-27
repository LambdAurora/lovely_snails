/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails.network;

import dev.lambdaurora.lovely_snails.LovelySnails;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the set storage page of a snail's container packet payload.
 *
 * @param syncId the synchronization identifier of the container
 * @param storagePage the selected storage page
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.2.0
 */
public record SnailSetStoragePagePayload(int syncId, byte storagePage) implements CustomPacketPayload {
	public static Type<SnailSetStoragePagePayload> TYPE = new Type<>(LovelySnails.id("snail_set_storage_page"));
	public static final StreamCodec<FriendlyByteBuf, SnailSetStoragePagePayload> STREAM_CODEC = CustomPacketPayload.codec(
			SnailSetStoragePagePayload::write, SnailSetStoragePagePayload::new
	);

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public SnailSetStoragePagePayload(FriendlyByteBuf buffer) {
		this(buffer.readVarInt(), buffer.readByte());
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(this.syncId);
		buffer.writeByte(this.storagePage);
	}

	static {
		PayloadTypeRegistry.playC2S().register(TYPE, STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(TYPE, STREAM_CODEC);
	}
}
