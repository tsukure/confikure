package re.tsuku.confikure.forge.internal.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import re.tsuku.confikure.forge.internal.ForgeEventBus;
import re.tsuku.fastbus.Subscribe;

public final class ForgeEventBusTest {
    @Test
    public void postsEventsToSubscribedListeners() {
        TickListener listener = new TickListener();
        ForgeEventBus.subscribe(listener);
        try {
            ForgeEventBus.postTick(EventPhase.PRE);
            assertEquals(1, listener.ticks);
        } finally {
            ForgeEventBus.unsubscribe(listener);
        }
    }

    private static final class TickListener {
        private int ticks;

        @Subscribe
        private void onTick(GameTickEvent event) {
            ticks++;
        }
    }
}
