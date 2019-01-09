/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import javax.xml.ws.WebServiceException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.stereotype.Component;


/**
 * Intercepts calls to methods (typically annotated with @StubModeAware)
 * and throws a WebServiceException if the stub is set to operate in {@link se.inera.intyg.webcert.intygstjanststub.mode.StubMode#OFFLINE}
 *
 * Created by erikl on 15-04-09.
 */
@Component
public class StubModeMethodInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (StubModeSingleton.getInstance().getStubMode() == StubMode.OFFLINE) {
            throw new WebServiceException("Stub is in OFFLINE mode");
        }

        // Artificially sleep if latency is set to other that 0L
//            if (StubModeSingleton.getInstance().getLatency() != 0L) {
//                Thread.sleep(StubModeSingleton.getInstance().getLatency() );
//            }
        return invocation.proceed();
    }
}
