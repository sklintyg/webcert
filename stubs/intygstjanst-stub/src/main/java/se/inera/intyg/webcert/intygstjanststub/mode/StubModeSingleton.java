package se.inera.intyg.webcert.intygstjanststub.mode;

/**
 * GoF-style JVM singleton for keeping track of the current @{StubMode} of the stub.
 *
 * Created by erikl on 15-04-09.
 */
public final class StubModeSingleton {

    private static StubMode stubMode = StubMode.ONLINE;
    private static long latency = 0L;

    private static volatile StubModeSingleton stubModeSingleton;

    private StubModeSingleton() {
    }

    public static StubModeSingleton getInstance() {
        if (stubModeSingleton == null) {
            synchronized (StubModeSingleton.class) {
                stubModeSingleton = new StubModeSingleton();
            }
        }
        return stubModeSingleton;
    }

    public void setStubMode(StubMode requestedStubMode) {
        stubMode = requestedStubMode;
    }

    public StubMode getStubMode() {
        return stubMode;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long requestedLatency) {
        latency = requestedLatency;
    }
}
