package re.tsuku.confikure.dev;

import re.tsuku.confikure.forge.event.ConfikureScreenOpenEvent;
import re.tsuku.fastbus.Subscribe;

final class DevEventListener {
    @Subscribe
    private void onScreenOpen(ConfikureScreenOpenEvent event) {
        System.out.println("[confikure-dev] opened " + event.screen().getClass().getSimpleName());
    }

    @Subscribe
    private void onOptionChanged(DevOptionChangedEvent event) {
        System.out.println("[confikure-dev] changed " + event.optionId() + " from " + event.oldValue() + " to "
                + event.newValue());
    }
}
