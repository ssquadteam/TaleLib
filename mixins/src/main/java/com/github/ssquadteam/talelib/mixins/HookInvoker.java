package com.github.ssquadteam.talelib.mixins;

import com.hypixel.hytale.logger.HytaleLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.logging.Level;

public final class HookInvoker {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private HookInvoker() {
    }

    public static Object invoke(String hookKey, String methodName, MethodType methodType, Object firstArg, Object... moreArgs) {
        try {
            Object bridge = System.getProperties().get(BridgeKeys.BRIDGE_KEY);
            if (!(bridge instanceof Map<?, ?> hooks)) return null;
            Object hook = hooks.get(hookKey);
            if (hook == null) return null;

            MethodHandle handle = MethodHandles.publicLookup().findVirtual(hook.getClass(), methodName, methodType);
            Object[] callArgs = new Object[moreArgs.length + 2];
            callArgs[0] = hook;
            callArgs[1] = firstArg;
            System.arraycopy(moreArgs, 0, callArgs, 2, moreArgs.length);
            return handle.invokeWithArguments(callArgs);
        } catch (Throwable e) {
            LOGGER.at(Level.WARNING).log("TaleLib mixin hook " + hookKey + "." + methodName + " failed: " + e.getMessage());
            return null;
        }
    }
}
