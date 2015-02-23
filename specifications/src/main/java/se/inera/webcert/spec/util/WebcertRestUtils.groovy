package se.inera.webcert.spec.util

import groovy.json.JsonBuilder
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC

public class WebcertRestUtils {
    private static final Logger LOG = LoggerFactory.getLogger(WebcertRestUtils.class)
    static final def webcert = new RESTClient("http://localhost:9088/")
        
    static def login(String user) {
        def logins = [:];
        logins["user1"] = '{"fornamn" : "Ivar", "efternamn" : "Integration", "hsaId" : "SE4815162344-1B01", "enhetId" : "SE4815162344-1A02", "lakare" : true,"forskrivarKod": "2481632"}'
        def loginData = logins[user]
        def postBody = 'userJsonDisplay=' + logins[user]
        def response = webcert.post(path: '/fake', body: postBody, requestContentType : URLENC )
        assert response.status == 302
        System.out.println("Using logindata: " + loginData)
    }
    
    public static HttpResponseDecorator createNewUtkast(String json) {
        def response = webcert.post(path: "api/utkast/fk7263", body: json, requestContentType: JSON)
        response
    }

    public static Collection getNotifications(int expected) {
        def notifieringar = []
        def startTime = System.currentTimeMillis()
        def runningTime = startTime
        def maxTime = 4000
        while (notifieringar.size < expected && (runningTime - startTime < maxTime)) {
            notifieringar = webcert.get(path: "services/notification-stub/notifieringar").data
            runningTime = System.currentTimeMillis()
        }
        notifieringar
    }

    public static HttpResponseDecorator saveUtkast(String intygsTyp, String utkastId, String json) {
        def response = webcert.put(path : "moduleapi/utkast/${intygsTyp}/${utkastId}", body : json, requestContentType : JSON)
        response
    }

    public static HttpResponseDecorator signUtkast(String intygsTyp, String intygsId) {
        def response = webcert.post(path: "moduleapi/utkast/${intygsTyp}/${intygsId}/signeraserver", requestContentType : JSON)
        response
    }

    public static HttpResponseDecorator sendIntyg(String intygsTyp, String intygsId, String recipientId) {
        def restPath = "/moduleapi/intyg/${intygsTyp}/${intygsId}/skicka"
        def response = webcert.post(path: restPath, body : """{"recipient": "${recipientId}", "patientConsent" : true }""", requestContentType : JSON)
        response
    }

    public static boolean reset() {
        def resp = webcert.post(path: "services/notification-stub/clear")
        return resp.success
    }
}
