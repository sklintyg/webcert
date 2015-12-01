package se.inera.webcert.spec.util

import groovyx.net.http.HttpResponseDecorator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

import static com.jayway.awaitility.Awaitility.await

/**
 * Created by eriklupander on 2015-05-26.
 *
 * Use this to perform asynchronous assertions vs the Intygstjanst Stub.
 */
class AsyncUtilsITStub extends RestClientFixture {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncUtilsITStub.class)


    static final String webcertBaseUrl = System.getProperty("webcert.baseUrl")
    static final def webcert = createRestClient(webcertBaseUrl)

    boolean result


    // INTYGSTJANST STUB
    boolean intygFinnsIStub(String intygsId, long timeout = 4000L) {
        result = false
        awaitResult(buildIntygExistsInStubConditionCallable(intygsId), timeout)
        result
    }

    boolean intygFinnsMarkeratSomSkickatIStub(String intygsId, String recipient, long timeout = 4000L) {
        result = false
        awaitResult(buildIntygExistsInStubAsSentConditionCallable(intygsId, recipient), timeout)
        result
    }

    boolean makuleratIntygFinnsIStub(String intygsId, long timeout = 4000L) {
        result = false
        awaitResult(buildMakuleratIntygExistsInStubConditionCallable(intygsId), timeout)
        result
    }



    // STUB PRIVATE
    private Callable<Boolean> buildIntygExistsInStubConditionCallable(String intygsId) {

        return {
            try {
                HttpResponseDecorator response = webcert.get(path : "/services/intygstjanst-stub/intyg/${intygsId}")
                result = !response.data.result.resultCode.equals('ERROR')
            } catch (Exception e) {
                result = false
            }
        }
    }

    private Callable<Boolean> buildMakuleratIntygExistsInStubConditionCallable(String intygsId) {

        return {
            try {
                HttpResponseDecorator response = webcert.get(path : "/services/intygstjanst-stub/intyg/${intygsId}")
                result = isRevoked(response.data.meta.status)
            } catch (Exception e) {
                result = false
            }
        }
    }

    private Callable<Boolean> buildIntygExistsInStubAsSentConditionCallable(String intygsId, String recipient) {
        return {
            try {
                HttpResponseDecorator response = webcert.get(path : "/services/intygstjanst-stub/intyg/${intygsId}")
                result = isSentToRecipientStub(recipient, response.data.meta.status)
                result
            } catch (Exception e) {
                result = false
            }
        }
    }

    private Callable<Integer> buildCountIntygInStubCallable() {
        return {
            try {
                HttpResponseDecorator response = webcert.get(path : "/services/intygstjanst-stub/intyg")
                result = response.data.length;
                result
            } catch (Exception e) {
                result = -1
            }
        }
    }

    private boolean isRevoked(states) {
        boolean revoked = false
        for(state in states) {
            if (state.type.equals('CANCELLED')) {
                revoked = true
            }
        }
        revoked
    }

    private boolean isSentToRecipientStub(recipient, states) {
        boolean sent = false
        for(state in states) {
            if (state.target.equals(recipient) && state.type.equals('SENT')) {
                sent = true
            }
        }
        sent
    }



}
