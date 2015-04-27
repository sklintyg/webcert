package se.inera.certificate.spec

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.core.io.ClassPathResource
import se.inera.certificate.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

public class Intyg extends RestClientFixture {

    String personnr
    String utfärdat
	String giltigtFrån
	String giltigtTill
    String utfärdarId
    String utfärdare
    String enhetsId = "1.2.3"
	String enhet
    String vårdgivarId
    String typ
    String id
    String idTemplate
	String mall = "M"
    int from
    int to
	private boolean skickat
	private boolean rättat
    private boolean deletedByCareGiver
    
	private String template
	
	public void setSkickat(String value) {
		skickat = value?.equalsIgnoreCase("ja")
	}

    public void setRättat(String value) {
        rättat = value?.equalsIgnoreCase("ja")
    }

    public void setBorttagetAvVården(String value) {
        deletedByCareGiver = value?.equalsIgnoreCase("ja")
    }

	public void reset() {
		mall = "M"
		utfärdarId = "EttUtfärdarId"
        utfärdare = "EnUtfärdare"
        enhetsId = "1.2.3"
		enhet = null
		giltigtFrån = null
		giltigtTill = null
		template = null
		skickat = false
		rättat = false
        deletedByCareGiver = false
	}
	
    public void execute() {
        def restClient = createRestClient()
        if (!giltigtFrån) giltigtFrån = utfärdat
        if (!enhet) enhet = enhetsId
		if (!giltigtTill) giltigtTill = new Date().parse("yyyy-MM-dd", utfärdat).plus(14).format("yyyy-MM-dd")
		if (from && to && !idTemplate) {
			template = "test-${personnr}-intyg-%1\$s"
		} else if (idTemplate) {
			template = idTemplate
		}
        for (int day in from..to) {
            if (template) {
                id = String.format(template, day, utfärdat, personnr, typ)
            }
            restClient.post(
                    path: 'certificate',
                    body: certificateJson(),
                    requestContentType: JSON
                    )
            utfärdat = new Date().parse("yyyy-MM-dd", utfärdat).plus(1).format("yyyy-MM-dd")
        }
    }

    private certificateJson() {
		def stateList = [[state:"RECEIVED", target:"MI", timestamp:utfärdat + "T12:00:00.000"]]
        if (skickat)
			stateList << [state:"SENT", target:"FK", timestamp:utfärdat + "T12:00:10.000"]
		if (rättat)
			stateList << [state:"CANCELLED", target:"MI", timestamp:utfärdat + "T13:00:00.000"]
        String additionalInfo = "";
        if (typ.equalsIgnoreCase("fk7263"))
            additionalInfo = "${giltigtFrån} - ${giltigtTill}"
        [id:String.format(id, utfärdat),
            type:typ.toLowerCase(),
            civicRegistrationNumber:personnr,
            signedDate:utfärdat,
            signingDoctorName: utfärdare,
            validFromDate:giltigtFrån,
            validToDate:giltigtTill,
            careUnitId: (enhetsId) ? enhetsId : "1.2.3",
            careUnitName: enhet,
            careGiverId: vårdgivarId ? vårdgivarId : "4.5.6",
            deletedByCareGiver : deletedByCareGiver,
            additionalInfo : additionalInfo,
			certificateStates: stateList,
            document: document()
        ]
    }

    protected document() {
        '"' + document(typ.toLowerCase()) + '"'
    }

    protected document(typ) {
        def certificate
        try {
            // slurping the FK7263 template
            certificate = new JsonSlurper().parse(new InputStreamReader(new ClassPathResource("${typ}_${mall}_template.json").getInputStream()))
        } catch (IOException e) {
            // if template for specific type cannot be loaded, use generic template
            certificate = new JsonSlurper().parse(new InputStreamReader(new ClassPathResource("generic_template.json").getInputStream()))
            certificate.typ = typ
        }

        // setting the certificate ID
        certificate.'id' = id

        // setting personnr in certificate XML
        certificate.grundData.patient.personId = personnr

        if (utfärdarId) certificate.grundData.skapadAv.personId = utfärdarId
		if (utfärdare) certificate.grundData.skapadAv.fullstandigtNamn = utfärdare
		if (enhetsId) certificate.grundData.skapadAv.vardenhet.enhetsid = enhetsId
		if (enhet) certificate.grundData.skapadAv.vardenhet.enhetsnamn = enhet

        if (vårdgivarId) certificate.grundData.skapadAv.vardenhet.vardgivare.vardgivarid = vårdgivarId
		
        // setting the signing date, from date and to date
        certificate.grundData.signeringsdatum = utfärdat

        if (typ == "fk7263") {
            if (certificate.undersokningAvPatienten) certificate.undersokningAvPatienten = utfärdat
            if (certificate.telefonkontaktMedPatienten) certificate.telefonkontaktMedPatienten = utfärdat
            if (certificate.journaluppgifter) certificate.journaluppgifter = utfärdat
            if (certificate.annanReferens) {
                 certificate.annanReferens = utfärdat
            } 
            if (certificate.nedsattMed100) {
                if (!certificate.nedsattMed100.from)
                    certificate.nedsattMed100.from = giltigtFrån
                if (!certificate.nedsattMed100.tom)
                    certificate.nedsattMed100.tom = giltigtTill
            } 
            if (certificate.nedsattMed75) {
                if (!certificate.nedsattMed75.from)
                    certificate.nedsattMed75.from = giltigtFrån
                if (!certificate.nedsattMed75.tom)
                    certificate.nedsattMed75.tom = giltigtTill
            } 
            if (certificate.nedsattMed50) {
                if (!certificate.nedsattMed50.from)
                    certificate.nedsattMed50.from = giltigtFrån
                if (!certificate.nedsattMed50.tom)
                    certificate.nedsattMed50.tom = giltigtTill
            } 
            if (certificate.nedsattMed25) {
                if (!certificate.nedsattMed25.from)
                    certificate.nedsattMed25.from = giltigtFrån
                if (!certificate.nedsattMed25.tom)
                    certificate.nedsattMed25.tom = giltigtTill
            } 
            certificate.giltighet.from = giltigtFrån
            certificate.giltighet.tom = giltigtTill
        }
        
        JsonOutput.toJson(certificate)
    }
}
