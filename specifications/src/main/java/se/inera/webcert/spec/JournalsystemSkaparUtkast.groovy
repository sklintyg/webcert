package se.inera.webcert.spec

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
import se.inera.webcert.spec.util.WsClientFixtureNyaKontraktet

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
