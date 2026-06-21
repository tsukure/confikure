package re.tsuku.confikure.forge.mixin.minecraft.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import re.tsuku.confikure.event.EventPhase;
import re.tsuku.confikure.forge.event.ConfikureEvents;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "runTick", at = @At("HEAD"))
    private void confikure$runTick$head(CallbackInfo callbackInfo) {
        ConfikureEvents.postTick(EventPhase.PRE);
    }

    @Inject(method = "runTick", at = @At("RETURN"))
    private void confikure$runTick$return(CallbackInfo callbackInfo) {
        ConfikureEvents.postTick(EventPhase.POST);
    }
}
