package com.github.ssquadteam.talelib.mixins;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

public final class MixinManifestConfig {

    @SerializedName("Configs")
    public Set<String> configs = new HashSet<>();
}
