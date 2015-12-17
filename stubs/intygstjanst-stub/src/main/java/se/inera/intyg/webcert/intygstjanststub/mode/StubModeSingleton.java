/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
