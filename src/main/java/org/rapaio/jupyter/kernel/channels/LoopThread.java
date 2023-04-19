package org.rapaio.jupyter.kernel.channels;

import java.util.logging.Logger;

public class LoopThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(LoopThread.class.getSimpleName());

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
                LOGGER.info(getName() + "Loop interrupted. Stopping...");
                running = false;
                break;
            }
        }
    }

    @Override
    public synchronized void start() {
        running = true;
        super.start();
        LOGGER.info(getName() + "Loop started.");
    }

    public void shutdown() {
        running = false;
        LOGGER.info(getName() + "Loop shutdown.");
    }
}
