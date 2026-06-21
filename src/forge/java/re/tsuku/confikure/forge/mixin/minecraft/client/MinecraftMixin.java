package re.tsuku.confikure.forge.mixin.minecraft.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import re.tsuku.confikure.forge.internal.ForgeEventBus;
import re.tsuku.confikure.forge.internal.event.EventPhase;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "runTick", at = @At("HEAD"))
    private void confikure$runTick$head(CallbackInfo callbackInfo) {
        ForgeEventBus.postTick(EventPhase.PRE);
    }

    @Inject(method = "runTick", at = @At("RETURN"))
    private void confikure$runTick$return(CallbackInfo callbackInfo) {
        ForgeEventBus.postTick(EventPhase.POST);
    }
}
