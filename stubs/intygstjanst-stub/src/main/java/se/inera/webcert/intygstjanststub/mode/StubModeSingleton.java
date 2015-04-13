package se.inera.webcert.intygstjanststub.mode;

/**
 * GoF-style JVM singleton for keeping track of the current @{StubMode} of the stub.
 *
 * Created by erikl on 15-04-09.
 */
public class StubModeSingleton {

    private static StubMode stubMode = StubMode.ONLINE;

    private volatile static StubModeSingleton stubModeSingleton;

    private StubModeSingleton() {
    }

    public static StubModeSingleton getInstance() {
        if(stubModeSingleton == null) {
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
}
