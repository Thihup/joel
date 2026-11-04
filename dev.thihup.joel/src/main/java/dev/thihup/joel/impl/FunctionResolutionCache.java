package dev.thihup.joel.impl;

import jakarta.el.FunctionMapper;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public final class FunctionResolutionCache {
    private static final int MAX_CACHE_SIZE = 4096;
    private static final ConcurrentHashMap<FunctionKey, ResolvedFunction> CACHE = new ConcurrentHashMap<>(256);

    public record ResolvedFunction(Method method, Class<?>[] parameterTypes) {}

    private record FunctionKey(FunctionMapper mapper, String prefix, String localName) {}

    private FunctionResolutionCache() {}

    public static ResolvedFunction resolve(FunctionMapper mapper, String prefix, String localName) {
        FunctionKey key = new FunctionKey(mapper, prefix, localName);
        ResolvedFunction resolved = CACHE.get(key);
        if (resolved != null) {
            return resolved;
        }

        Method method = mapper.resolveFunction(prefix, localName);
        if (method == null) {
            return null;
        }

        try {
            method.trySetAccessible();
        } catch (Exception ignored) {
        }

        resolved = new ResolvedFunction(method, method.getParameterTypes());
        if (CACHE.size() >= MAX_CACHE_SIZE) {
            CACHE.clear();
        }
        CACHE.putIfAbsent(key, resolved);
        return resolved;
    }
}

