package re.tsuku.confikure.forge.event;

import re.tsuku.confikure.event.EventPhase;
import re.tsuku.confikure.event.GameTickEvent;
import re.tsuku.confikure.forge.ConfikureForgeScreen;
import re.tsuku.fastbus.Event;
import re.tsuku.fastbus.FastBus;

public final class ConfikureEvents {
    private static final FastBus BUS = new FastBus();

    private ConfikureEvents() {
    }

    public static void subscribe(Object listener) {
        BUS.subscribe(listener);
    }

    public static void unsubscribe(Object listener) {
        BUS.unsubscribe(listener);
    }

    public static void postTick(EventPhase phase) {
        post(new GameTickEvent(phase));
    }

    public static void postScreenOpen(ConfikureForgeScreen screen) {
        post(new ConfikureScreenOpenEvent(screen));
    }

    static void post(Event event) {
        BUS.post(event);
    }
}
