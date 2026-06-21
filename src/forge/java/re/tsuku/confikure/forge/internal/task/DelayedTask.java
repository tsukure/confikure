package re.tsuku.confikure.forge.internal.task;

final class DelayedTask {
    private int ticks;
    private final Runnable runnable;

    DelayedTask(int ticks, Runnable runnable) {
        this.ticks = ticks;
        this.runnable = runnable;
    }

    boolean tick() {
        return ticks-- <= 0;
    }

    void run() {
        runnable.run();
    }
}
