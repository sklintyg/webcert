package se.inera.webcert.intygstjanststub.mode;

import javax.xml.ws.WebServiceException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.stereotype.Component;


/**
 * Intercepts calls to methods (typically annotated with @StubModeAware)
 * and throws a WebServiceException if the stub is set to operate in {@link se.inera.webcert.intygstjanststub.mode.StubMode#OFFLINE}
 *
 * Created by erikl on 15-04-09.
 */
@Component
public class StubModeMethodInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        try {
            if (StubModeSingleton.getInstance().getStubMode() == StubMode.OFFLINE) {
                throw new WebServiceException("Stub is in OFFLINE mode");
            }
            return invocation.proceed();
        } finally {
        }
    }
}
