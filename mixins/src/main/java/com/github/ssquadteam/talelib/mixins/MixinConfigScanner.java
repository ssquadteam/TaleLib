package com.github.ssquadteam.talelib.mixins;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

public final class MixinConfigScanner {

    private static final Gson GSON = new Gson();

    private final Map<Path, PluginManifest> manifests = new HashMap<>();

    public MixinConfigScanner scan(Path directory) {
        if (!Files.isDirectory(directory)) return this;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.jar")) {
            for (Path jar : stream) {
                try {
                    PluginManifest manifest = readManifest(jar);
                    if (manifest != null) {
                        manifests.put(jar, manifest);
                    }
                } catch (IOException e) {
                    MixinRuntime.log("Failed to read manifest from " + jar.getFileName() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            MixinRuntime.log("Failed to scan " + directory + ": " + e.getMessage());
        }
        return this;
    }

    public Map<Path, PluginManifest> manifests() {
        return Collections.unmodifiableMap(manifests);
    }

    private static PluginManifest readManifest(Path jar) throws IOException {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            var entry = zip.getEntry("manifest.json");
            if (entry == null) return null;
            try (Reader reader = new InputStreamReader(zip.getInputStream(entry))) {
                return GSON.fromJson(reader, PluginManifest.class);
            }
        }
    }
}
