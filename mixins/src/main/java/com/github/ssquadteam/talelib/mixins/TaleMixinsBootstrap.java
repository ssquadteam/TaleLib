package com.github.ssquadteam.talelib.mixins;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public final class TaleMixinsBootstrap implements IMixinServiceBootstrap {

    @Override
    public String getName() {
        return MixinRuntime.NAME;
    }

    @Override
    public String getServiceClassName() {
        return "com.github.ssquadteam.talelib.mixins.TaleMixinsService";
    }

    @Override
    public void bootstrap() {
    }
}
