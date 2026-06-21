package re.tsuku.confikure.forge.internal;

import re.tsuku.confikure.forge.ForgeConfigScreen;
import re.tsuku.confikure.forge.internal.event.EventPhase;
import re.tsuku.confikure.forge.internal.event.GameTickEvent;
import re.tsuku.confikure.forge.internal.event.ScreenOpenEvent;
import re.tsuku.fastbus.Event;
import re.tsuku.fastbus.FastBus;

public final class Events {
    private static final FastBus BUS = new FastBus();

    private Events() {
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

    public static void postScreenOpen(ForgeConfigScreen screen) {
        post(new ScreenOpenEvent(screen));
    }

    static void post(Event event) {
        BUS.post(event);
    }
}
