/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Lovely Snails.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lovely_snails;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;

import java.util.ArrayList;
import java.util.List;

public class SnailContainer extends SimpleContainer {
	private final List<Listener> listeners = new ArrayList<>();

	public SnailContainer(int size) {
		super(size);
	}

	public void addListener(Listener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public void setChanged() {
		super.setChanged();
		for (var listener : this.listeners) {
			listener.containerChanged(this);
		}
	}

	public interface Listener {
		void containerChanged(Container container);
	}
}
