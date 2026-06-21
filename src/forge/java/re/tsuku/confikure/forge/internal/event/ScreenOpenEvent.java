package re.tsuku.confikure.forge.internal.event;

import re.tsuku.confikure.forge.ForgeConfigScreen;
import re.tsuku.fastbus.Event;

public final class ScreenOpenEvent implements Event {
    private final ForgeConfigScreen screen;

    public ScreenOpenEvent(ForgeConfigScreen screen) {
        this.screen = screen;
    }

    public ForgeConfigScreen screen() {
        return screen;
    }
}
