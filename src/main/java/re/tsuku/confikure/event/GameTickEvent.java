package re.tsuku.confikure.event;

import re.tsuku.fastbus.Event;

public final class GameTickEvent implements Event {
    private final EventPhase phase;

    public GameTickEvent(EventPhase phase) {
        this.phase = phase;
    }

    public EventPhase getPhase() {
        return phase;
    }
}
