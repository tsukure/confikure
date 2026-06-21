package re.tsuku.confikure.example;

import re.tsuku.fastbus.Event;

final class ThemeChangedEvent implements Event {
    private final Object oldTheme;
    private final Object newTheme;

    ThemeChangedEvent(Object oldTheme, Object newTheme) {
        this.oldTheme = oldTheme;
        this.newTheme = newTheme;
    }

    Object oldTheme() {
        return oldTheme;
    }

    Object newTheme() {
        return newTheme;
    }
}
