package re.tsuku.confikure.example;

import re.tsuku.fastbus.Subscribe;

final class ExampleEvents {
    @Subscribe
    private void onThemeChanged(ThemeChangedEvent event) {
        System.out.println("[confikure-example] theme changed from " + event.oldTheme() + " to " + event.newTheme());
    }
}
