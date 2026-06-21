package re.tsuku.confikure.forge.internal.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import re.tsuku.confikure.forge.internal.event.EventPhase;
import re.tsuku.confikure.forge.internal.event.GameTickEvent;

public final class DelayedTasksTest {
    @Test
    public void runsTasksAfterRequestedPreTicks() {
        DelayedTasks handler = new DelayedTasks();
        final int[] runs = {0};

        handler.scheduleTask(1, () -> runs[0]++);

        handler.tick(new GameTickEvent(EventPhase.POST));
        assertEquals(0, runs[0]);

        handler.tick(new GameTickEvent(EventPhase.PRE));
        assertEquals(0, runs[0]);

        handler.tick(new GameTickEvent(EventPhase.PRE));
        assertEquals(1, runs[0]);

        handler.tick(new GameTickEvent(EventPhase.PRE));
        assertEquals(1, runs[0]);
    }

    @Test
    public void zeroTickTasksRunOnNextPreTick() {
        DelayedTasks handler = new DelayedTasks();
        final int[] runs = {0};

        handler.scheduleTask(0, () -> runs[0]++);
        handler.tick(new GameTickEvent(EventPhase.PRE));

        assertEquals(1, runs[0]);
    }

    @Test
    public void defersTasksScheduledWhileTicking() {
        DelayedTasks handler = new DelayedTasks();
        final int[] runs = {0};

        handler.scheduleTask(0, () -> {
            runs[0]++;
            handler.scheduleTask(0, () -> runs[0] += 10);
        });

        handler.tick(new GameTickEvent(EventPhase.PRE));
        assertEquals(1, runs[0]);

        handler.tick(new GameTickEvent(EventPhase.PRE));
        assertEquals(11, runs[0]);
    }

    @Test
    public void rejectsInvalidTasks() {
        DelayedTasks handler = new DelayedTasks();

        assertThrows(IllegalArgumentException.class, () -> handler.scheduleTask(-1, () -> {
        }));
        assertThrows(NullPointerException.class, () -> handler.scheduleTask(0, null));
    }
}
