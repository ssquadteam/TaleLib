package com.github.ssquadteam.talelib.mixins;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

public final class PluginManifest {

    @SerializedName("TaleMixins")
    public MixinManifestConfig taleMixins;

    public Set<String> mixinConfigs() {
        return taleMixins != null ? taleMixins.configs : Set.of();
    }
}
