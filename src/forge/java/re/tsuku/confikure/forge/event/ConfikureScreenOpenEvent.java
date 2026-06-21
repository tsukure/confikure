package re.tsuku.confikure.forge.event;

import re.tsuku.confikure.forge.ConfikureForgeScreen;
import re.tsuku.fastbus.Event;

public final class ConfikureScreenOpenEvent implements Event {
    private final ConfikureForgeScreen screen;

    public ConfikureScreenOpenEvent(ConfikureForgeScreen screen) {
        this.screen = screen;
    }

    public ConfikureForgeScreen screen() {
        return screen;
    }
}
