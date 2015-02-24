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

    /**
     * Create a new Utkast
     * @param intygTyp the type of intyg to create, used to invoke the correct module api
     * @param json the json body of the request
     * @return HttpResponseDecorator
     */
    public static HttpResponseDecorator createNewUtkast(String intygTyp, String json) {
        def response = webcert.post(path: "api/utkast/${intygTyp}", body: json, requestContentType: JSON)
        response
    }

    /**
     * Get the expected number of notifications present in the stub,
     * timeout if this takes more than 4 seconds.
     * 
     * @param expected the number of expected notifications to get
     * @return a collection of notifications
     */
    public static Collection getNotifications(int expected) {
        def notifieringar = []
        def startTime = System.currentTimeMillis()
        def runningTime = startTime
        def maxTime = 4000
        while (notifieringar.size < expected && runningTime - startTime < maxTime) {
            notifieringar = webcert.get(path: "services/notification-stub/notifieringar").data
            runningTime = System.currentTimeMillis()
        }
        notifieringar
    }

    /**
     * Save Utkast.
     * 
     * @param intygsTyp
     * @param utkastId
     * @param json
     * @return HttpResponseDecorator
     */
    public static HttpResponseDecorator saveUtkast(String intygsTyp, String utkastId, String json) {
        def response = webcert.put(path : "moduleapi/utkast/${intygsTyp}/${utkastId}", body : json, requestContentType : JSON)
        response
    }

    /**
     * Sign a complete utkast
     * @param intygsTyp
     * @param intygsId
     * @return HttpResponseDecorator
     */
    public static HttpResponseDecorator signUtkast(String intygsTyp, String intygsId) {
        def response = webcert.post(path: "moduleapi/utkast/${intygsTyp}/${intygsId}/signeraserver", requestContentType : JSON)
        response
    }

    /**
     * Send an Intyg to a recipient.
     * @param intygsTyp 
     * @param intygsId
     * @param recipientId id of the recipient (i.e FK)
     * @return HttpResponseDecorator
     */
    public static HttpResponseDecorator sendIntyg(String intygsTyp, String intygsId, String recipientId) {
        def restPath = "/moduleapi/intyg/${intygsTyp}/${intygsId}/skicka"
        def response = webcert.post(path: restPath, body : """{"recipient": "${recipientId}", "patientConsent" : true }""", requestContentType : JSON)
        response
    }

    /**
     * Create a new question on an Intyg.
     * @param intygsTyp
     * @param intygsId
     * @param questionJson
     * @return HttpResponseDecorator
     */
    public static HttpResponseDecorator createQuestionToFk(String intygsTyp, String intygsId, String questionJson) {
        def restPath= "/moduleapi/fragasvar/${intygsTyp}/${intygsId}"
        def response = webcert.post(path : restPath, body : questionJson, requestContentType : JSON)
        response
    }

    /**
     * Delete a draft
     * @param intygsTyp
     * @param utkastId
     * @return HttpResponseDecorator
     */
    public static HttpResponseDecorator deleteUtkast(String intygsTyp, String utkastId) {
        def restPath ="/moduleapi/utkast/${intygsTyp}/${utkastId}"
        def response = webcert.delete(path : restPath)
        response
    }

    /**
     * Delete and recall a signed Intyg.
     * @param intygsTyp
     * @param intygsId
     * @return HttpResponseDecorator
     */
    public static HttpResponseDecorator deleteIntyg(String intygsTyp, String intygsId) {
        def restPath = "/moduleapi/intyg/${intygsTyp}/${intygsId}/aterkalla"
        def response = webcert.post(path : restPath, requestContentType : JSON, body : "")
        response
    }

    /**
     * Clear all notifications in the stub
     * @return true if call was successful, false otherwise
     */
    public static boolean reset() {
        def resp = webcert.post(path: "services/notification-stub/clear")
        return resp.success
    }
}
