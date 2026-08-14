package com.github.ssquadteam.talelib.mixins.impl;

import com.github.ssquadteam.talelib.mixins.BridgeKeys;
import com.github.ssquadteam.talelib.mixins.HookInvoker;

import com.hypixel.hytale.common.util.ListUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.ForkedChainId;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.invoke.MethodType;
import java.util.List;

@Mixin(InteractionManager.class)
public abstract class InteractionManagerHooks {

    @Shadow
    private boolean hasRemoteClient;

    @Shadow
    public abstract void sendCancelPacket(int chainId, ForkedChainId forkedChainId);

    @Shadow
    @Final
    private PlayerRef playerRef;

    @Shadow
    @Final
    private ObjectList<SyncInteractionChain> syncPackets;

    @Shadow
    private static SyncInteractionChain makeSyncPacket(InteractionChain chain, int operationBaseIndex, List<InteractionSyncData> interactionData) {
        return null;
    }

    @Unique
    private boolean isChainGuarded(InteractionChain chain) {
        Object result = HookInvoker.invoke(
            BridgeKeys.CHAIN_GUARD_HOOK,
            "isChainGuarded",
            MethodType.methodType(boolean.class, InteractionChain.class),
            chain
        );
        return Boolean.TRUE.equals(result);
    }

    @Inject(method = "tickChain", at = @At("HEAD"))
    private void beforeTickChain(InteractionChain chain, CallbackInfoReturnable<Boolean> cir) {
        if (isChainGuarded(chain)) {
            this.hasRemoteClient = false;
        }
    }

    @Inject(method = "tickChain", at = @At("RETURN"))
    private void afterTickChain(InteractionChain chain, CallbackInfoReturnable<Boolean> cir) {
        if (isChainGuarded(chain)) {
            this.hasRemoteClient = true;
        }
    }

    @Inject(method = "doTickChain", at = @At("HEAD"))
    private void beforeDoTickChain(Ref<EntityStore> ref, InteractionChain chain, CallbackInfo ci) {
        if (isChainGuarded(chain)) {
            this.hasRemoteClient = false;
        }
    }

    @Inject(method = "doTickChain", at = @At("RETURN"))
    private void afterDoTickChain(Ref<EntityStore> ref, InteractionChain chain, CallbackInfo ci) {
        if (isChainGuarded(chain)) {
            this.hasRemoteClient = true;
        }
    }

    /**
     * @author SSQuadTeam
     * @reason Keep locally-driven bridge chains out of the client cancel path.
     */
    @Overwrite
    private void sendCancelPacket(InteractionChain chain) {
        if (isChainGuarded(chain)) return;
        this.sendCancelPacket(chain.getChainId(), chain.getForkedChainId());
    }

    /**
     * @author SSQuadTeam
     * @reason Suppress sync packets for locally-driven bridge chains.
     */
    @Overwrite
    public void sendSyncPacket(InteractionChain chain, int operationBaseIndex, List<InteractionSyncData> interactionData) {
        if (isChainGuarded(chain)) return;
        if (chain.hasSentInitial()
            && (interactionData == null || ListUtil.emptyOrAllNull(interactionData))
            && chain.getNewForks().isEmpty()) {
            return;
        }
        if (this.playerRef != null) {
            SyncInteractionChain packet = makeSyncPacket(chain, operationBaseIndex, interactionData);
            this.syncPackets.add(packet);
        }
    }
}
