package re.tsuku.confikure.forge.mixin.minecraft.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import re.tsuku.confikure.Confikure;
import re.tsuku.confikure.event.EventPhase;
import re.tsuku.confikure.event.GameTickEvent;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "runTick", at = @At("HEAD"))
    private void confikure$runTick$head(CallbackInfo callbackInfo) {
        Confikure.eventBus().post(new GameTickEvent(EventPhase.PRE));
    }

    @Inject(method = "runTick", at = @At("RETURN"))
    private void confikure$runTick$return(CallbackInfo callbackInfo) {
        Confikure.eventBus().post(new GameTickEvent(EventPhase.POST));
    }
}
