package se.inera.intyg.webcert.web.integration.builder;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.integration.hsa.services.HsaOrganizationsService;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;
import se.inera.intyg.webcert.web.service.dto.Vardenhet;
import se.inera.intyg.webcert.web.service.dto.Vardgivare;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Patient;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;
import se.riv.infrastructure.directory.v1.CommissionType;

@Component
public class CreateNewDraftRequestBuilderImpl implements CreateNewDraftRequestBuilder {

    private static final String SPACE = " ";

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Override
    public CreateNewDraftRequest buildCreateNewDraftRequest(Utlatande utlatande, CommissionType miuOnUnit) {
        CreateNewDraftRequest utkastsRequest = new CreateNewDraftRequest();

        utkastsRequest.setIntygType(utlatande.getTypAvUtlatande().getCode());

        se.inera.intyg.webcert.web.service.dto.Patient patient = createPatient(utlatande.getPatient());
        utkastsRequest.setPatient(patient);

        Vardenhet vardenhet = createVardenhetFromMIU(miuOnUnit);
        utkastsRequest.setVardenhet(vardenhet);

        HoSPerson hosPerson = createHoSPerson(utlatande.getSkapadAv());
        utkastsRequest.setHosPerson(hosPerson);
        return utkastsRequest;
    }

    private Vardenhet createVardenhetFromMIU(CommissionType miu) {

        se.inera.intyg.webcert.integration.hsa.model.Vardenhet hsaVardenhet = hsaOrganizationsService.getVardenhet(miu.getHealthCareUnitHsaId());

        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setNamn(hsaVardenhet.getNamn());
        vardenhet.setHsaId(hsaVardenhet.getId());
        vardenhet.setArbetsplatskod(hsaVardenhet.getArbetsplatskod());
        vardenhet.setPostadress(hsaVardenhet.getPostadress());
        vardenhet.setPostnummer(hsaVardenhet.getPostnummer());
        vardenhet.setPostort(hsaVardenhet.getPostort());
        vardenhet.setTelefonnummer(hsaVardenhet.getTelefonnummer());

        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setHsaId(miu.getHealthCareProviderHsaId());
        vardgivare.setNamn(miu.getHealthCareProviderName());
        vardenhet.setVardgivare(vardgivare);

        return vardenhet;
    }

    private HoSPerson createHoSPerson(HosPersonal hoSPersonType) {
        HoSPerson hoSPerson = new HoSPerson();
        hoSPerson.setNamn(hoSPersonType.getFullstandigtNamn());
        hoSPerson.setHsaId(hoSPersonType.getPersonalId().getExtension());
        hoSPerson.setForskrivarkod(hoSPerson.getForskrivarkod());
        return hoSPerson;
    }

    private se.inera.intyg.webcert.web.service.dto.Patient createPatient(Patient patientType) {
        se.inera.intyg.webcert.web.service.dto.Patient patient = new se.inera.intyg.webcert.web.service.dto.Patient();
        patient.setPersonnummer(new Personnummer(patientType.getPersonId().getExtension()));
        patient.setFornamn(joinNames(patientType.getFornamn()));
        patient.setMellannamn(joinNames(patientType.getMellannamn()));
        patient.setEfternamn(patientType.getEfternamn());
        return patient;
    }

    private static String joinNames(List<String> names) {
        return StringUtils.join(names, SPACE);
    }
}
