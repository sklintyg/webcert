/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.common.specifications.spec

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.core.io.ClassPathResource
import se.inera.intyg.common.specifications.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON
import org.joda.time.LocalDateTime
import org.springframework.core.io.ClassPathResource
import se.inera.intyg.common.specifications.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType
import se.inera.intygstjanster.ts.services.RegisterTSBasResponder.v1.RegisterTSBasType
import se.inera.intygstjanster.ts.services.RegisterTSDiabetesResponder.v1.RegisterTSDiabetesType

import javax.xml.transform.stream.StreamSource
import java.io.StringWriter
import javax.xml.bind.*

public class Intyg extends RestClientFixture {

    String personnr
	String patientNamn
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
	
	String responseStatus;
    
	private String template

    def request
	
	public void setSkickat(String value) {
		skickat = value?.equalsIgnoreCase("ja")
	}

    public void setRättat(String value) {
        rättat = value?.equalsIgnoreCase("ja")
    }

    public void setBorttagetAvVården(String value) {
        deletedByCareGiver = value?.equalsIgnoreCase("ja")
    }
	
	public String respons() {
		return responseStatus;
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
            def resp = restClient.post(
                    path: 'certificate',
                    body: certificateJson(),
                    requestContentType: JSON
                    )
			
			responseStatus = resp.status
            utfärdat = new Date().parse("yyyy-MM-dd", utfärdat).plus(1).format("yyyy-MM-dd")
        }
    }

    private certificateJson() {
        def doc = document()
		def stateList = [[state:"RECEIVED", target:"HV", timestamp:utfärdat + "T12:00:00.000"]]
        if (skickat)
			stateList << [state:"SENT", target:"FK", timestamp:utfärdat + "T12:00:10.000"]
		if (rättat)
			stateList << [state:"CANCELLED", target:"HV", timestamp:utfärdat + "T13:00:00.000"]
        String additionalInfo = "";
        if (typ.equalsIgnoreCase("fk7263"))
            additionalInfo = "${giltigtFrån} - ${giltigtTill}"
        else if (typ.equalsIgnoreCase("ts-bas") || typ.equalsIgnoreCase("ts-diabetes")) {
            def korkortstyper = request.intyg.intygAvser.korkortstyp*.value.value
            additionalInfo = "${korkortstyper.join(', ')}"
        }
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
            originalCertificate: doc
        ]
    }

    protected document() {
        '"' + document(typ.toLowerCase()) + '"'
    }

    protected document(typ) {
        if (typ == "ts-bas") {
            JAXBContext jaxbContext = JAXBContext.newInstance(RegisterTSBasType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            request = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("${typ}_${mall}_template.xml").getInputStream()), RegisterTSBasType.class).getValue()

            request.intyg.intygsId = id
            request.intyg.grundData.patient.personId.extension = personnr

            if (patientNamn) request.intyg.grundData.patient.fullstandigtNamn = patientNamn
            if (utfärdarId) request.intyg.grundData.skapadAv.personId.extension = utfärdarId
            if (utfärdare) request.intyg.grundData.skapadAv.fullstandigtNamn = utfärdare
            if (enhetsId) request.intyg.grundData.skapadAv.vardenhet.enhetsId.extension = enhetsId
            if (enhet) request.intyg.grundData.skapadAv.vardenhet.enhetsnamn = enhet
            if (vårdgivarId) request.intyg.grundData.skapadAv.vardenhet.vardgivare.vardgivarid.extension = vårdgivarId
            request.intyg.grundData.signeringsTidstampel = utfärdat
        } else if (typ == "ts-diabetes") {
            JAXBContext jaxbContext = JAXBContext.newInstance(RegisterTSDiabetesType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            request = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("${typ}_${mall}_template.xml").getInputStream()), RegisterTSDiabetesType.class).getValue()

            request.intyg.intygsId = id
            request.intyg.grundData.patient.personId.extension = personnr

            if (patientNamn) request.intyg.grundData.patient.fullstandigtNamn = patientNamn
            if (utfärdarId) request.intyg.grundData.skapadAv.personId.extension = utfärdarId
            if (utfärdare) request.intyg.grundData.skapadAv.fullstandigtNamn = utfärdare
            if (enhetsId) request.intyg.grundData.skapadAv.vardenhet.enhetsId.extension = enhetsId
            if (enhet) request.intyg.grundData.skapadAv.vardenhet.enhetsnamn = enhet
            if (vårdgivarId) request.intyg.grundData.skapadAv.vardenhet.vardgivare.vardgivarid.extension = vårdgivarId
            request.intyg.grundData.signeringsTidstampel = utfärdat
        } else {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                request = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("${typ}_${mall}_template.xml").getInputStream()), RegisterMedicalCertificateType.class).getValue()
                // slurping the FK7263 template
                //certificate = new JsonSlurper().parse(new InputStreamReader(new ClassPathResource("${typ}_${mall}_template.xml").getInputStream()))
            } catch (Exception e) {
                JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                request = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("generic_template.xml").getInputStream()), RegisterMedicalCertificateType.class).getValue()
                // if template for specific type cannot be loaded, use generic template
                //certificate = new JsonSlurper().parse(new InputStreamReader(new ClassPathResource("generic_template.xml").getInputStream()))
                //certificate.typ = typ
            }
            request.lakarutlatande.lakarutlatandeId = id
            request.lakarutlatande.patient.personId.extension = personnr

            if (patientNamn) request.lakarutlatande.patient.fullstandigtNamn = patientNamn
            if (utfärdarId) request.lakarutlatande.skapadAvHosPersonal.personalId.extension = utfärdarId
            if (utfärdare) request.lakarutlatande.skapadAvHosPersonal.fullstandigtNamn = utfärdare
            if (enhetsId) request.lakarutlatande.skapadAvHosPersonal.enhet.enhetsId.extension = enhetsId
            if (enhet) request.lakarutlatande.skapadAvHosPersonal.enhet.enhetsnamn = enhet
            if (vårdgivarId) request.lakarutlatande.skapadAvHosPersonal.enhet.vardgivare.vardgivareId.extension = vårdgivarId
            request.lakarutlatande.signeringsdatum = LocalDateTime.parse(utfärdat)
        }

        StringWriter writer = new StringWriter()
        JAXB.marshal(request, writer)
        return writer.toString()
    }
}
