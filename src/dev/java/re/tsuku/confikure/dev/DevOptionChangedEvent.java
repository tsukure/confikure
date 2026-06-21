package re.tsuku.confikure.dev;

import re.tsuku.fastbus.Event;

final class DevOptionChangedEvent implements Event {
    private final String optionId;
    private final Object oldValue;
    private final Object newValue;

    DevOptionChangedEvent(String optionId, Object oldValue, Object newValue) {
        this.optionId = optionId;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    String optionId() {
        return optionId;
    }

    Object oldValue() {
        return oldValue;
    }

    Object newValue() {
        return newValue;
    }
}
