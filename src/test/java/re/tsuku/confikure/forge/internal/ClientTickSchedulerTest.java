package re.tsuku.confikure.forge.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.junit.Test;

public final class ClientTickSchedulerTest {
    @Test
    public void runsTaskOnceOnClientTickStart() {
        final int[] runs = {0};
        ClientTickScheduler.ScheduledTask task = new ClientTickScheduler.ScheduledTask(() -> runs[0]++, false);

        task.onClientTick(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
        assertEquals(0, runs[0]);

        task.onClientTick(new TickEvent.ClientTickEvent(TickEvent.Phase.START));
        assertEquals(1, runs[0]);

        task.onClientTick(new TickEvent.ClientTickEvent(TickEvent.Phase.START));
        assertEquals(1, runs[0]);
    }

    @Test
    public void rejectsNullTasks() {
        assertThrows(NullPointerException.class, () -> new ClientTickScheduler.ScheduledTask(null, false));
    }
}
