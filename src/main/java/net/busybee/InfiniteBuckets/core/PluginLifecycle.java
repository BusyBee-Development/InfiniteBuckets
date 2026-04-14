package net.busybee.InfiniteBuckets.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class PluginLifecycle {

    private final AtomicLong generation = new AtomicLong();
    private final AtomicReference<State> state = new AtomicReference<>(State.STOPPED);

    public long beginStartup() {
        long nextGeneration = generation.incrementAndGet();
        state.set(State.STARTING);
        return nextGeneration;
    }

    public void markRunning() {
        state.set(State.RUNNING);
    }

    public long beginShutdown() {
        long nextGeneration = generation.incrementAndGet();
        state.set(State.STOPPING);
        return nextGeneration;
    }

    public void markStopped() {
        state.set(State.STOPPED);
    }
    public long getGeneration() {
        return generation.get();
    }
    public State getState() {
        return state.get();
    }

    public boolean isActive(long expectedGeneration) {
        State currentState = state.get();
        return generation.get() == expectedGeneration
            && (currentState == State.STARTING || currentState == State.RUNNING);
    }

    public boolean isStoppingOrStopped() {
        State currentState = state.get();
        return currentState == State.STOPPING || currentState == State.STOPPED;
    }

    public enum State {
        STARTING,
        RUNNING,
        STOPPING,
        STOPPED
    }
}
