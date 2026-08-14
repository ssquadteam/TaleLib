package com.github.ssquadteam.talelib.mixins;

import org.objectweb.asm.ClassReader;
import sun.misc.Unsafe;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;

public final class MixinClassLoaders {
    private static MixinClassLoaders instance;
    private static MethodHandles.Lookup trustedLookup;

    private final ClassLoader pluginLoader;
    private final ClassLoader appLoader;
    private ClassLoader runtimeLoader;

    static {
        trustedLookup = obtainTrustedLookup();
        if (trustedLookup != null) {
            MixinRuntime.log("Obtained trusted lookup via Unsafe");
        }
    }

    @SuppressWarnings("removal")
    private static MethodHandles.Lookup obtainTrustedLookup() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            long fieldOffset = unsafe.staticFieldOffset(implLookupField);
            Object fieldBase = unsafe.staticFieldBase(implLookupField);
            return (MethodHandles.Lookup) unsafe.getObject(fieldBase, fieldOffset);
        } catch (Exception e) {
            MixinRuntime.log("Failed to obtain trusted lookup: " + e.getMessage());
            return null;
        }
    }

    private MixinClassLoaders(ClassLoader pluginLoader, ClassLoader appLoader) {
        this.pluginLoader = pluginLoader;
        this.appLoader = appLoader;
    }

    public static MixinClassLoaders get() {
        if (instance == null) {
            throw new IllegalStateException("MixinClassLoaders has not been created yet");
        }
        return instance;
    }

    public static void create(ClassLoader pluginLoader, ClassLoader appLoader) {
        if (instance != null) {
            throw new IllegalStateException("MixinClassLoaders has already been created");
        }
        instance = new MixinClassLoaders(pluginLoader, appLoader);
    }

    public ClassLoader getRuntimeLoader() {
        return runtimeLoader;
    }

    public void captureRuntimeLoader(ClassLoader loader) {
        if (runtimeLoader != null) {
            throw new IllegalStateException("Runtime loader has already been captured: " + runtimeLoader);
        }
        runtimeLoader = loader;
        injectJarUrls(loader);
    }

    private void injectJarUrls(ClassLoader loader) {
        if (trustedLookup == null || !(loader instanceof URLClassLoader target)) {
            MixinRuntime.log("Skipping jar injection: no trusted lookup or loader is not a URLClassLoader");
            return;
        }
        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            addUrlQuietly(target, codeSource.getLocation());
        }
        if (pluginLoader instanceof URLClassLoader urlLoader) {
            for (URL url : urlLoader.getURLs()) {
                addUrlQuietly(target, url);
            }
        }
    }

    private void addUrlQuietly(URLClassLoader loader, URL url) {
        try {
            addUrl(loader, url);
            MixinRuntime.log("Injected " + url);
        } catch (Throwable e) {
            MixinRuntime.log("Failed to inject " + url + ": " + e.getMessage());
        }
    }

    private static void addUrl(URLClassLoader loader, URL url) throws Throwable {
        MethodHandle addUrl = trustedLookup.findVirtual(
            URLClassLoader.class,
            "addURL",
            MethodType.methodType(void.class, URL.class)
        );
        addUrl.invoke(loader, url);
    }

    public ClassLoader findLoaderForClass(String className) throws ClassNotFoundException {
        try {
            return findLoaderFor(className.replace('.', '/') + ".class");
        } catch (IOException e) {
            throw new ClassNotFoundException("Could not find class '" + className + "'", e);
        }
    }

    public ClassLoader findLoaderFor(String resourceName) throws IOException {
        if (runtimeLoader != null && runtimeLoader.getResource(resourceName) != null) {
            return runtimeLoader;
        }
        if (pluginLoader != null && pluginLoader.getResource(resourceName) != null) {
            return pluginLoader;
        }
        if (appLoader != null && appLoader.getResource(resourceName) != null) {
            return appLoader;
        }
        throw new FileNotFoundException("Could not find resource '" + resourceName + "' on any class loader");
    }

    public InputStream findResourceStream(String resourceName) throws IOException {
        return findLoaderFor(resourceName).getResourceAsStream(resourceName);
    }

    public ClassReader getClassReader(String className) throws IOException {
        String fileName = className.replace('.', '/') + ".class";
        try (InputStream stream = findResourceStream(fileName)) {
            return new ClassReader(stream);
        }
    }
}
