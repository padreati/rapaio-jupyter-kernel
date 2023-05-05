package org.rapaio.jupyter.kernel.core.java;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import jdk.jshell.execution.DirectExecutionControl;
import jdk.jshell.spi.SPIResolutionException;

public class RapaioExecutionControl extends DirectExecutionControl {

    public static final String TIMEOUT_MARKER = "## Timeout Marker ##";
    public static final String INTERRUPTED_MARKER = "## Interrupted Marker ##";
    private static final Object NULL = new Object();
    private static final AtomicInteger ID = new AtomicInteger(0);

    private final ExecutorService executor;
    private final long timeoutTime;
    private final ConcurrentMap<String, Future<Object>> running = new ConcurrentHashMap<>();
    private final Map<String, Object> results = new ConcurrentHashMap<>();

    public RapaioExecutionControl(long timeoutTime) {
        this.timeoutTime = timeoutTime;
        this.executor = Executors.newCachedThreadPool(r -> new Thread(r, "Engine-thread-" + ID.getAndIncrement()));
    }

    public long getTimeout() {
        return timeoutTime;
    }

    public Object takeResult(String key) {
        Object result = this.results.remove(key);
        if (result == null) {
            throw new IllegalStateException("No result with key: " + key);
        }
        return result == NULL ? null : result;
    }

    @Override
    protected String invoke(Method methodCall) throws Exception {
        String id = UUID.randomUUID().toString();
        Object value = execute(id, methodCall);
        results.put(id, value);
        return id;
    }

    private Object execute(String id, Method methodCall) throws Exception {
        Future<Object> runningTask = this.executor.submit(() -> methodCall.invoke(null));

        running.put(id, runningTask);

        try {
            if (timeoutTime > 0) {
                return runningTask.get(this.timeoutTime, TimeUnit.MILLISECONDS);
            }
            return runningTask.get();
        } catch (CancellationException e) {
            if (executor.isShutdown()) {
                throw new StoppedException();
            } else {
                throw new UserException("Execution interrupted.", INTERRUPTED_MARKER, e.getStackTrace());
            }
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof InvocationTargetException) {
                cause = cause.getCause();
            }
            if (cause == null) {
                throw new UserException("null", "Unknown Invocation Exception", e.getStackTrace());
            } else if (cause instanceof SPIResolutionException spiResCause) {
                throw new ResolutionException(spiResCause.id(), cause.getStackTrace());
            } else {
                throw new UserException(cause.getMessage(), cause.getClass().getName(), cause.getStackTrace());
            }
        } catch (TimeoutException e) {
            throw new UserException("Execution timed out", TIMEOUT_MARKER, e.getStackTrace()
            );
        } finally {
            running.remove(id, runningTask);
        }
    }

    public void interrupt() {
        running.forEach((id, future) -> future.cancel(true));
    }

    @Override
    public void stop() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
