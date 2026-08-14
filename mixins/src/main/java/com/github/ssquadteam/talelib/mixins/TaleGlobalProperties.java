package com.github.ssquadteam.talelib.mixins;

import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

import java.util.HashMap;
import java.util.Map;

public final class TaleGlobalProperties implements IGlobalPropertyService {

    private final Map<String, IPropertyKey> keys = new HashMap<>();
    private final Map<IPropertyKey, Object> values = new HashMap<>();

    @Override
    public IPropertyKey resolveKey(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Property names must not be null or empty");
        }
        return keys.computeIfAbsent(name, Key::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(IPropertyKey key) {
        return (T) values.get(key);
    }

    @Override
    public void setProperty(IPropertyKey key, Object value) {
        values.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(IPropertyKey key, T defaultValue) {
        return (T) values.getOrDefault(key, defaultValue);
    }

    @Override
    public String getPropertyString(IPropertyKey key, String defaultValue) {
        return getProperty(key, defaultValue);
    }

    private record Key(String name) implements IPropertyKey {
    }
}
