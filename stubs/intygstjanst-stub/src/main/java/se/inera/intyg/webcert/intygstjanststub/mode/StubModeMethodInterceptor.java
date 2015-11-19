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
