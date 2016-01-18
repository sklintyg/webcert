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

package se.inera.intyg.webcert.specifications.spec.api

import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.CreateDraftCertificateResponderInterface
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.CreateDraftCertificateResponseType
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.CreateDraftCertificateType
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Enhet
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonal
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Patient
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande
import se.riv.clinicalprocess.healthcond.certificate.types.v1.HsaId
import se.riv.clinicalprocess.healthcond.certificate.types.v1.PersonId
import se.riv.clinicalprocess.healthcond.certificate.types.v1.TypAvUtlatande
import se.inera.intyg.webcert.specifications.spec.util.WsClientFixtureNyaKontraktet

/**
 * @author andreaskaltenbach
 */
class JournalsystemSkaparUtkast extends WsClientFixtureNyaKontraktet {

    private def createDraftResponder

    String personId
    String fornamn = "forNamn"
    String mellannamn
    String efternamn = "efterNamn"
    String hsaId = "hsaId"
    String hsaNamn = "hsaNamn"
    String enhetsId = "enhetsId"
    String enhetsNamn = "enhetsNamn"
    String typAvUtlatande = "fk7263"
    
    public JournalsystemSkaparUtkast() {
        super()
    }

    public JournalsystemSkaparUtkast(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
        String serviceUrl = System.getProperty("service.createDraftUrl")
        String url = serviceUrl ? serviceUrl : baseUrl + "services/create-draft-certificate/v1.0"
        createDraftResponder = createClient(CreateDraftCertificateResponderInterface.class, url)
    }

    private CreateDraftCertificateResponseType response
    
    def execute() {
        Utlatande utlatande = new Utlatande()
        utlatande.typAvUtlatande = new TypAvUtlatande()
        utlatande.typAvUtlatande.code = typAvUtlatande
        utlatande.typAvUtlatande.displayName = typAvUtlatande
        utlatande.typAvUtlatande.codeSystem ="f6fb361a-e31d-48b8-8657-99b63912dd9b"
        utlatande.typAvUtlatande.codeSystemName = "kv_utlåtandetyp_intyg"
        utlatande.patient = new Patient()
        utlatande.patient.personId = new PersonId()
        utlatande.patient.personId.extension = personId
        utlatande.patient.getFornamn().add(fornamn)
        if (mellannamn) {
            utlatande.patient.getMellannamn().add(mellannamn)
        }
        utlatande.patient.setEfternamn(efternamn)
        utlatande.skapadAv = new HosPersonal()
        utlatande.skapadAv.personalId = new HsaId()
        utlatande.skapadAv.personalId.extension = hsaId
        utlatande.skapadAv.fullstandigtNamn = hsaNamn
        utlatande.skapadAv.enhet = new Enhet()
        utlatande.skapadAv.enhet.enhetsId = new HsaId()
        utlatande.skapadAv.enhet.enhetsId.extension = enhetsId
        utlatande.skapadAv.enhet.enhetsnamn = enhetsNamn
        CreateDraftCertificateType draftCertificate = new CreateDraftCertificateType()
        draftCertificate.utlatande = utlatande

        response = createDraftResponder.createDraftCertificate(logicalAddress.value, draftCertificate);
    }
    
    String utkastId() {
        response.utlatandeId.extension
    }
    
    String resultat() {
        resultAsString(response)
    }
}
