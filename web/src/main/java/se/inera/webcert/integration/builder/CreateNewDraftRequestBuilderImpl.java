package se.inera.webcert.integration.builder;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonalType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.PatientType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.UtlatandeType;
import se.inera.ifv.hsawsresponder.v3.MiuInformationType;
import se.inera.webcert.hsa.services.HsaOrganizationsService;
import se.inera.webcert.service.draft.dto.CreateNewDraftRequest;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.dto.Patient;
import se.inera.webcert.service.dto.Vardenhet;
import se.inera.webcert.service.dto.Vardgivare;

@Component
public class CreateNewDraftRequestBuilderImpl implements CreateNewDraftRequestBuilder {
	
	private static final Logger LOG = LoggerFactory.getLogger(CreateNewDraftRequestBuilderImpl.class);
	
	private static final String SPACE = " ";
	
	@Autowired
	private HsaOrganizationsService hsaOrganizationsService;

	@Override
	public CreateNewDraftRequest buildCreateNewDraftRequest(UtlatandeType utlatande, MiuInformationType miuOnUnit) {
		CreateNewDraftRequest utkastsRequest = new CreateNewDraftRequest();
						
		utkastsRequest.setIntygType(utlatande.getTypAvUtlatande().getCode());
		
		Patient patient = createPatient(utlatande.getPatient());
		utkastsRequest.setPatient(patient);
		
		Vardenhet vardenhet = createVardenhetFromMIU(miuOnUnit);
		utkastsRequest.setVardenhet(vardenhet);
				
		HoSPerson hosPerson = createHoSPerson(utlatande.getSkapadAv());
		utkastsRequest.setHosPerson(hosPerson);
		return utkastsRequest;
	}

	private Vardenhet createVardenhetFromMIU(MiuInformationType miu) {
		
		se.inera.webcert.hsa.model.Vardenhet hsaVardenhet = hsaOrganizationsService.getVardenhet(miu.getCareUnitHsaIdentity());
		
		Vardenhet vardenhet = new Vardenhet();
		vardenhet.setNamn(hsaVardenhet.getNamn());
		vardenhet.setHsaId(hsaVardenhet.getId());
		vardenhet.setArbetsplatskod(hsaVardenhet.getArbetsplatskod());
		vardenhet.setPostadress(hsaVardenhet.getPostadress());
		vardenhet.setPostnummer(hsaVardenhet.getPostnummer());
		vardenhet.setPostort(hsaVardenhet.getPostort());
		vardenhet.setTelefonnummer(hsaVardenhet.getTelefonnummer());
		
		Vardgivare vardgivare = new Vardgivare();
		vardgivare.setHsaId(miu.getCareGiver());
		vardgivare.setNamn(miu.getCareGiverName());
		vardenhet.setVardgivare(vardgivare);
		
		return vardenhet;
	}

	private HoSPerson createHoSPerson(HosPersonalType hoSPersonType) {
		HoSPerson hoSPerson = new HoSPerson();
		hoSPerson.setNamn(hoSPersonType.getFullstandigtNamn());
		hoSPerson.setHsaId(hoSPersonType.getPersonalId().getExtension());
		hoSPerson.setForskrivarkod(hoSPerson.getForskrivarkod());
		return hoSPerson;
	}

	private Patient createPatient(PatientType patientType) {
		Patient patient = new Patient();
		patient.setPersonnummer(patientType.getPersonId().getExtension());
		patient.setFornamn(joinNames(patientType.getFornamn()));
		patient.setMellannamn(joinNames(patientType.getMellannamn()));
		patient.setEfternamn(patientType.getEfternamn());
		return patient;
	}
	
	private static String joinNames(List<String> names) {
		return StringUtils.join(names, SPACE);
	}
}
