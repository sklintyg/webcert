package se.inera.webcert.spec.util
/**
 *
 * @author andreaskaltenbach
 */
class RestClientFixture extends se.inera.certificate.spec.util.RestClientFixture{

    String baseUrl = System.getProperty("webcert.baseUrl")
    String logSenderBaseUrl = System.getProperty("logsender.baseUrl")
    String intygsTjanstBaseUrl = System.getProperty("certificate.baseUrl")
}
