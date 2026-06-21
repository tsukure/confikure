package re.tsuku.confikure.forge.internal.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import re.tsuku.confikure.forge.internal.Events;
import re.tsuku.fastbus.Subscribe;

public final class EventsTest {
    @Test
    public void postsEventsToSubscribedListeners() {
        TickListener listener = new TickListener();
        Events.subscribe(listener);
        try {
            Events.postTick(EventPhase.PRE);
            assertEquals(1, listener.ticks);
        } finally {
            Events.unsubscribe(listener);
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
