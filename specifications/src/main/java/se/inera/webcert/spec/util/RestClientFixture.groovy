package se.inera.webcert.spec.util

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

import static com.jayway.awaitility.Awaitility.await

/**
 *
 * @author andreaskaltenbach
 */
class RestClientFixture extends se.inera.certificate.spec.util.RestClientFixture {

    String baseUrl = System.getProperty("webcert.baseUrl")
    String logSenderBaseUrl = System.getProperty("logsender.baseUrl")

    protected void awaitResult(Callable<Boolean> callable, long timeout) {
        await().atMost(timeout, TimeUnit.MILLISECONDS).until(callable)
    }
}
