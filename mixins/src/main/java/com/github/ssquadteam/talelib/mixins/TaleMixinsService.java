package com.github.ssquadteam.talelib.mixins;

import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.IMixinAuditTrail;
import org.spongepowered.asm.service.IMixinInternal;
import org.spongepowered.asm.service.ITransformerProvider;
import org.spongepowered.asm.service.MixinServiceAbstract;
import org.spongepowered.asm.util.IConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Collection;
import java.util.List;

public final class TaleMixinsService extends MixinServiceAbstract {

    public static IMixinTransformer transformer;
    private static IConsumer<MixinEnvironment.Phase> phaseConsumer;

    private final IClassProvider classProvider = new TaleClassProvider();
    private final IClassBytecodeProvider bytecodeProvider = new TaleBytecodeProvider();

    @Override
    public String getName() {
        return MixinRuntime.NAME;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public IClassProvider getClassProvider() {
        return classProvider;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return bytecodeProvider;
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return null;
    }

    @Override
    public IClassTracker getClassTracker() {
        return null;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return List.of("com.github.ssquadteam.talelib.mixins.TalePlatformAgent");
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        URI location = jarLocation();
        return location != null ? new ContainerHandleURI(location) : new ContainerHandleVirtual(getName());
    }

    private static URI jarLocation() {
        CodeSource codeSource = TaleMixinsService.class.getProtectionDomain().getCodeSource();
        if (codeSource == null) return null;
        try {
            return codeSource.getLocation().toURI();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public void offer(IMixinInternal internal) {
        if (internal instanceof IMixinTransformerFactory factory) {
            transformer = factory.createTransformer();
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        try {
            return MixinClassLoaders.get().findResourceStream(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void wire(MixinEnvironment.Phase phase, IConsumer<MixinEnvironment.Phase> phaseConsumer) {
        super.wire(phase, phaseConsumer);
        TaleMixinsService.phaseConsumer = phaseConsumer;
    }

    public static void changePhase(MixinEnvironment.Phase phase) {
        phaseConsumer.accept(phase);
    }
}
