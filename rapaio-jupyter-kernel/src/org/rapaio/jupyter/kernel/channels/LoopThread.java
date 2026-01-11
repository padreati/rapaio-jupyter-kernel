package org.rapaio.jupyter.kernel.channels;

/**
 * Thread which runs in loop the loop body after each waiting period.
 */
public final class LoopThread extends Thread {

    private volatile boolean running = false;
    private final long sleep;
    private final Runnable loopBody;

    public LoopThread(String name, long sleep, Runnable loopBody) {
        super(name);
        this.sleep = sleep;
        this.loopBody = loopBody;
    }

    @Override
    public void run() {
        while (running) {
            loopBody.run();
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                running = false;
                return;
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start();
        running = true;
    }

    public void shutdown() {
        running = false;
    }
}
