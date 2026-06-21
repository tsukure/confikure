package re.tsuku.confikure.forge.internal;

import java.util.Objects;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class ClientTickScheduler {
    private ClientTickScheduler() {
    }

    public static void schedule(Runnable runnable) {
        ScheduledTask task = new ScheduledTask(runnable, true);
        MinecraftForge.EVENT_BUS.register(task);
    }

    static final class ScheduledTask {
        private final Runnable runnable;
        private final boolean registered;
        private boolean ran;

        ScheduledTask(Runnable runnable, boolean registered) {
            this.runnable = Objects.requireNonNull(runnable, "runnable");
            this.registered = registered;
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.START || ran) {
                return;
            }
            ran = true;
            if (registered) {
                MinecraftForge.EVENT_BUS.unregister(this);
            }
            runnable.run();
        }
    }
}
