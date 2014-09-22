package com.grack.homomorphic.engine;

import com.grack.homomorphic.ops.NativeBitFactory;
import com.grack.homomorphic.ops.State;
import com.grack.homomorphic.ops.StateFactory;

public interface Engine {
	/**
	 * Initialize the HME inter-tick state.
	 */
	void initialize(NativeBitFactory factory, StateFactory stateFactory);

	/**
	 * Tick the engine. Depending on the implementation of the
	 * {@link NativeBitFactory}, this may be a direct execution or will generate
	 * a graph of operations.
	 */
	void tick(State state);
}
