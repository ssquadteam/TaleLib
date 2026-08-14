package com.github.ssquadteam.talelib.mixins;

import org.spongepowered.asm.service.IClassProvider;

import java.net.URL;

public final class TaleClassProvider implements IClassProvider {

    @Override
    public URL[] getClassPath() {
        return new URL[0];
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return MixinClassLoaders.get().findLoaderForClass(name).loadClass(name);
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, MixinClassLoaders.get().findLoaderForClass(name));
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return findClass(name, initialize);
    }
}
