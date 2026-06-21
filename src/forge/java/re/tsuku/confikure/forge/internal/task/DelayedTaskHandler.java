package re.tsuku.confikure.forge.internal.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import re.tsuku.confikure.forge.internal.event.EventPhase;
import re.tsuku.confikure.forge.internal.event.GameTickEvent;
import re.tsuku.fastbus.Subscribe;

public final class DelayedTaskHandler {
    private static final DelayedTaskHandler INSTANCE = new DelayedTaskHandler();

    private final List<DelayedTask> tasks = new ArrayList<>();
    private final List<DelayedTask> pendingTasks = new ArrayList<>();
    private boolean ticking;

    DelayedTaskHandler() {
    }

    public static DelayedTaskHandler get() {
        return INSTANCE;
    }

    public static void schedule(int ticks, Runnable runnable) {
        INSTANCE.scheduleTask(ticks, runnable);
    }

    @Subscribe
    private void onTick(GameTickEvent event) {
        tick(event);
    }

    void tick(GameTickEvent event) {
        if (event.getPhase() != EventPhase.PRE) {
            return;
        }
        ticking = true;
        try {
            Iterator<DelayedTask> iterator = tasks.iterator();
            while (iterator.hasNext()) {
                DelayedTask task = iterator.next();
                if (task.tick()) {
                    iterator.remove();
                    task.run();
                }
            }
        } finally {
            ticking = false;
            flushPendingTasks();
        }
    }

    void scheduleTask(int ticks, Runnable runnable) {
        if (ticks < 0) {
            throw new IllegalArgumentException("ticks must be non-negative");
        }
        DelayedTask task = new DelayedTask(ticks, Objects.requireNonNull(runnable, "runnable"));
        if (ticking) {
            pendingTasks.add(task);
            return;
        }
        tasks.add(task);
    }

    private void flushPendingTasks() {
        if (pendingTasks.isEmpty()) {
            return;
        }
        tasks.addAll(pendingTasks);
        pendingTasks.clear();
    }
}
