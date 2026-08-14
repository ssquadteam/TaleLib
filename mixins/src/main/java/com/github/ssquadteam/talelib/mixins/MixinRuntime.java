package com.github.ssquadteam.talelib.mixins;

public final class MixinRuntime {
    public static final String NAME = "TaleMixins";

    private MixinRuntime() {
    }

    public static void log(String message) {
        System.out.println("[" + NAME + "] " + message);
    }
}
