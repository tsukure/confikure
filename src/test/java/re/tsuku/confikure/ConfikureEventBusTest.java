package re.tsuku.confikure;

import static org.junit.Assert.assertSame;

import org.junit.Test;

public final class ConfikureEventBusTest {
    @Test
    public void exposesSingleSharedEventBus() {
        assertSame(Confikure.eventBus(), Confikure.eventBus());
    }
}
