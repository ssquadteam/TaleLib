package com.github.ssquadteam.talelib.mixins.impl;

import com.github.ssquadteam.talelib.mixins.BridgeKeys;
import com.github.ssquadteam.talelib.mixins.HookInvoker;

import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.auth.AuthConfig;
import com.hypixel.hytale.server.core.auth.ProfileServiceClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.io.IOException;
import java.lang.invoke.MethodType;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

@Mixin(ProfileServiceClient.class)
public abstract class ProfileServiceHooks {

    @Shadow
    @Final
    private static HytaleLogger LOGGER;

    @Shadow
    @Final
    private HttpClient httpClient;

    @Shadow
    @Final
    private String profileServiceUrl;

    @Unique
    private ProfileServiceClient.PublicGameProfile resolvePrefixedProfile(String username) {
        Object result = HookInvoker.invoke(
            BridgeKeys.PROFILE_RESOLVER_HOOK,
            "resolveProfile",
            MethodType.methodType(ProfileServiceClient.PublicGameProfile.class, String.class),
            username
        );
        return (ProfileServiceClient.PublicGameProfile) result;
    }

    /**
     * @author SSQuadTeam
     * @reason Resolve prefixed bridge player profiles before delegating to the profile service.
     */
    @SuppressWarnings("deprecation")
    @Overwrite
    public ProfileServiceClient.PublicGameProfile getProfileByUsername(String username, String bearerToken) {
        ProfileServiceClient.PublicGameProfile hooked = resolvePrefixedProfile(username);
        if (hooked != null) return hooked;

        try {
            String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.profileServiceUrl + "/profile/username/" + encodedUsername))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + bearerToken)
                .header("User-Agent", AuthConfig.USER_AGENT)
                .timeout(AuthConfig.HTTP_TIMEOUT)
                .GET()
                .build();
            LOGGER.at(Level.FINE).log("Fetching profile by username: %s", username);
            HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOGGER.at(Level.WARNING).log("Failed to fetch profile by username: HTTP %d - %s", response.statusCode(), response.body());
                return null;
            }
            ProfileServiceClient.PublicGameProfile profile = ProfileServiceClient.PublicGameProfile.CODEC.decodeJson(
                new RawJsonReader(response.body().toCharArray()),
                EmptyExtraInfo.EMPTY
            );
            if (profile == null) {
                LOGGER.at(Level.WARNING).log("Profile Service returned invalid response for username: %s", username);
                return null;
            }
            return profile;
        } catch (IOException e) {
            LOGGER.at(Level.WARNING).log("IO error while fetching profile by username: %s", e.getMessage());
            return null;
        } catch (InterruptedException e) {
            LOGGER.at(Level.WARNING).log("Request interrupted while fetching profile by username");
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).log("Unexpected error fetching profile by username: %s", e.getMessage());
            return null;
        }
    }
}
