package se.inera.webcert.spec

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient
import org.springframework.core.io.ClassPathResource
import se.inera.webcert.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON
/**
 * Created by pehr on 9/23/13.
 */
public class TaBortFraga extends RestClientFixture{

    String externReferens;

    public void execute() {
        def restClient = new RESTClient(baseUrl)
        Exception pendingException
        String failedIds = ""

            try {
                restClient.delete(
                        path: 'questions/' + externReferens,
                        requestContentType: JSON
                )
            } catch(e) {
                failedIds + ","
                if (!pendingException) {
                    pendingException = e
                }
            }
        if (pendingException) {
            throw new Exception("Kunde inte ta bort " + failedIds, pendingException)
        }
    }



}

