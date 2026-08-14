package com.github.ssquadteam.talelib.mixins;

import com.hypixel.hytale.plugin.early.ClassTransformer;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.nio.file.Path;

public final class MixinLaunchTransformer implements ClassTransformer {

    public MixinLaunchTransformer() {
        MixinClassLoaders.create(getClass().getClassLoader(), Thread.currentThread().getContextClassLoader());
        System.setProperty("java.util.logging.manager", "com.hypixel.hytale.logger.backend.HytaleLogManager");
        System.setProperty("mixin.bootstrapService", "com.github.ssquadteam.talelib.mixins.TaleMixinsBootstrap");
        System.setProperty("mixin.service", "com.github.ssquadteam.talelib.mixins.TaleMixinsService");

        MixinConfigScanner scanner = new MixinConfigScanner();
        scanner.scan(Path.of("earlyplugins"));
        MixinBootstrap.init();
        scanner.manifests().forEach((jar, manifest) -> {
            for (String config : manifest.mixinConfigs()) {
                MixinRuntime.log("Loading mixin config '" + config + "' from '" + jar.getFileName() + "'");
                Mixins.addConfiguration(config);
            }
        });
    }

    private void setupRuntimeEnvironment() {
        MixinClassLoaders.get().captureRuntimeLoader(Thread.currentThread().getContextClassLoader());
        TaleMixinsService.changePhase(MixinEnvironment.Phase.INIT);
        TaleMixinsService.changePhase(MixinEnvironment.Phase.DEFAULT);
    }

    @Override
    public int priority() {
        return -100;
    }

    @Override
    public byte[] transform(String name, String path, byte[] bytes) {
        if (MixinClassLoaders.get().getRuntimeLoader() == null) {
            setupRuntimeEnvironment();
        }
        return TaleMixinsService.transformer.transformClassBytes(name, name, bytes);
    }
}
