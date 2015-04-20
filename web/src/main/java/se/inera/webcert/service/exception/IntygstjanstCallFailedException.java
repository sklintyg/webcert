package se.inera.webcert.service.exception;

import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;

/**
 * @author andreaskaltenbach
 */
public class IntygstjanstCallFailedException extends RuntimeException {

    private final ResultType result;

    public IntygstjanstCallFailedException(ResultType result) {
        this.result = result;
    }

    @Override
    public String getMessage() {
        return "Failed to invoke web service method on intygstjänst. Result of call is " + result;
    }

    public ResultType getResult() {
        return result;
    }
}
