package se.inera.webcert.spec.util

import se.inera.certificate.spec.Browser

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC
import groovy.json.JsonOutput
import groovyx.net.http.HttpResponseDecorator

import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class WebcertRestUtils extends RestClientFixture {
    private static final Logger LOG = LoggerFactory.getLogger(WebcertRestUtils.class)
    static final String webCertBaseUrl = System.getProperty("webcert.baseUrl")
    static final def webcert = createRestClient(webCertBaseUrl)

    static def login() {
        login(webcert)
    }

    static def login(def restClient, String hsaId = "SE4815162344-1B01", String enhetId = "SE4815162344-1A02", boolean lakare = true) {
        def loginData = JsonOutput.toJson([fornamn: 'fornamn', efternamn: 'efternamn', hsaId: hsaId, enhetId: enhetId, lakare: lakare, forskrivarKod: "2481632"])
        def response = restClient.post(path: '/fake', body: "userJsonDisplay=${loginData}", requestContentType : URLENC )
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
        if (expected > 0) {
            while (notifieringar.size < expected && runningTime - startTime < maxTime) {
                notifieringar = webcert.get(path: "services/notification-stub/notifieringar").data
                runningTime = System.currentTimeMillis()
            }
        } else {
            while (runningTime - startTime < maxTime) {
                notifieringar = webcert.get(path: "services/notification-stub/notifieringar").data
                runningTime = System.currentTimeMillis()
            }
        }
        notifieringar
    }

    /**
     *  Check if a notification with the specified id and code is present in the stub.
     *  
     *  @param id the utlatandeId
     *  @param code, the code for the notification
     *  @param timeOutMillis, timout after this many millis
     *  @return true if a matching notification is found, false othewise 
     */
    public static boolean awaitNotification(final String id, final String code, final long timeOutMillis) {
        final long timeOut = System.currentTimeMillis() + timeOutMillis;

        while (System.currentTimeMillis() < timeOut) {
            def notifieringar = webcert.get(path: "services/notification-stub/notifieringar").data

            if(notifieringar.find {  it.utlatande.utlatandeId.extension == id && it.utlatande.handelse.handelsekod.code == code }){
                return true;
            }
        }
        return false;
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
     * Mark a question from FK as 'handled'.
     * @param intygsTyp the type of intyg (i.e fk7263)
     * @param internReferens the internal referens to the question
     * @return HttpResponseDecorator
     */
    public static HttpResponseDecorator setQuestionAsAnswered(String intygsTyp, String internReferens) {
        def restPath= "/moduleapi/fragasvar/${intygsTyp}/${internReferens}/stang"
        def response = webcert.get(path : restPath) 
        response 
    }

    /**
     * Mark a question from FK as not 'handled'.
     * @param intygsTyp the type of intyg (i.e fk7263)
     * @param internReferens the internal referens to the question
     * @return HttpResponseDecorator
     */
    public static HttpResponseDecorator setQuestionAsUnhandled(String intygsTyp, String internReferens) {
        def restPath= "/moduleapi/fragasvar/${intygsTyp}/${internReferens}/oppna"
        def response = webcert.get(path : restPath) 
        response 
    }

    /**
     * 
     * @param intygsTyp
     * @param internReferens
     * @param json
     * @return
     */
    public static HttpResponseDecorator answerQuestion(String intygsTyp, String internReferns, String text) {
        def restPath ="/moduleapi/fragasvar/${intygsTyp}/${internReferns}/besvara"
        def response = webcert.put(path : restPath, body : text, requestContentType : JSON)
        response
    }

    public static String translateExternalToInternalReferens(String externReferens) {
        def restPath ="/services/questions/extern/${externReferens}/translate"
        def response = webcert.get(path : restPath, requestContentType : JSON)
        response.data.internReferens
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

    /**
     * Get the number of unsigned certificates for currently logged in user.
     */
    public static Integer getNumberOfUnsignedCertificates() {
        def restPath = "/api/utkast"
        def response = webcert.get(path : restPath, requestContentType : JSON, query: ["complete":"false"],
                headers: ["Cookie":"JSESSIONID="+Browser.getJSession()])
        return response.data.totalCount;
    }

    /**
     * Instructs the Stub for Intygstjänsten (if active) to go into either ONLINE (normal operation) or OFFLINE (will throw
     * WebServiceException for annotated methods) mode.
     *
     * @param mode ONLINE or OFFLINE
     * @return true if mode change successful.
     */
    public static boolean setIntygTjanstStubInMode(String mode) {
        def restPath = "/services/intygstjanst-stub/mode/" + mode
        def resp = webcert.put(path: restPath)
        return resp.success
    }
}
