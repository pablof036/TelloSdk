package io.github.pablof036.tellosdk.api;

import java.util.ArrayList;
import java.util.function.Consumer;

class Listener<T> {
    private final ArrayList<Consumer<T>> listeners = new ArrayList<>();

    public void addListener(Consumer<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<T> listener) {
        listeners.remove(listener);
    }

    public void push(T value) {
        listeners.forEach(c -> c.accept(value));
    }
}
