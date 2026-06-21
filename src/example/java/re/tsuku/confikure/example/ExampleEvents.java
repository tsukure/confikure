package re.tsuku.confikure.example;

import re.tsuku.fastbus.Subscribe;

final class ExampleEvents {
    @Subscribe
    private void onThemeChanged(ThemeChangedEvent event) {
        ExampleMod.LOGGER.info("theme changed from {} to {}", event.oldTheme(), event.newTheme());
    }
}
