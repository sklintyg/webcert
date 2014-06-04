package se.inera.webcert.overvakning;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.persistence.intyg.repository.OmsandningRepository;

public class HealthCheck {
    private static final int NANOS_PER_MS = 1_000_000;

    @Autowired
    private HSAWebServiceCalls hsaService;

    @Autowired
    private IntygRepository intygRepository;

    @Autowired
    private OmsandningRepository omsandningRepository;

    public Status getPing() {
        boolean ok;
        long startTime = System.nanoTime();
        try {
            ok = true;
        } catch (Exception e) {
            ok = false;
        }
        long doneTime = System.nanoTime();
        return createStatus(ok, startTime, doneTime);
    }

    public Status getHsaStatus() {
        boolean ok;
        long startTime = System.nanoTime();
        try {
            hsaService.callPing();
            ok = true;
        } catch (Exception e) {
            ok = false;
        }
        long doneTime = System.nanoTime();
        return createStatus(ok, startTime, doneTime);
    }

    public Status getDbStatus() {
        boolean ok;
        long startTime = System.nanoTime();
        try {
            intygRepository.count();
            ok = true;
        } catch (Exception e) {
            ok = false;
        }
        long doneTime = System.nanoTime();
        return createStatus(ok, startTime, doneTime);
    }

    public Status getSignaturQueueSize() {
        boolean ok;
        long size = -1;
        try {
            size = omsandningRepository.count();
            ok = true;
        } catch (Exception e) {
            ok = false;
        }
        return new Status(size, ok);
    }

    private Status createStatus(boolean ok, long startTime, long doneTime) {
        return new Status((doneTime - startTime) / NANOS_PER_MS, ok);
    }

    public static final class Status {
        private final long measurement;
        private final boolean ok;

        private Status(long measurement, boolean ok) {
            this.measurement = measurement;
            this.ok = ok;
        }

        public boolean isOk() {
            return ok;
        }

        public long getMeasurement() {
            return measurement;
        }
    }
}
