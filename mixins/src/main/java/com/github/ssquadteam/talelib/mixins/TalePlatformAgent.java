package com.github.ssquadteam.talelib.mixins;

import org.spongepowered.asm.launch.platform.IMixinPlatformServiceAgent;
import org.spongepowered.asm.launch.platform.MixinPlatformAgentAbstract;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.util.Constants;

import java.util.Collection;
import java.util.List;

public final class TalePlatformAgent extends MixinPlatformAgentAbstract implements IMixinPlatformServiceAgent {

    @Override
    public void init() {
    }

    @Override
    public String getSideName() {
        return Constants.SIDE_UNKNOWN;
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return List.of();
    }
}
