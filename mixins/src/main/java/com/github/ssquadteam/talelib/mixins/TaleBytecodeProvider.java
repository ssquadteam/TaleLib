package com.github.ssquadteam.talelib.mixins;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.IClassBytecodeProvider;

import java.io.IOException;

public final class TaleBytecodeProvider implements IClassBytecodeProvider {

    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return getClassNode(name, true);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        return getClassNode(name, runTransformers, 0);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers, int readerFlags) throws ClassNotFoundException, IOException {
        ClassReader reader = MixinClassLoaders.get().getClassReader(name);
        ClassNode node = new ClassNode();
        reader.accept(node, readerFlags);
        return node;
    }
}
