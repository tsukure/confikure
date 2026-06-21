package re.tsuku.confikure.forge.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import re.tsuku.confikure.event.EventPhase;
import re.tsuku.confikure.event.GameTickEvent;
import re.tsuku.fastbus.Subscribe;

public final class ConfikureEventsTest {
    @Test
    public void postsEventsToSubscribedListeners() {
        TickListener listener = new TickListener();
        ConfikureEvents.subscribe(listener);
        try {
            ConfikureEvents.postTick(EventPhase.PRE);
            assertEquals(1, listener.ticks);
        } finally {
            ConfikureEvents.unsubscribe(listener);
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
