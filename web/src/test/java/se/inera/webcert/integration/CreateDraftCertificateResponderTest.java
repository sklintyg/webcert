package se.inera.webcert.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.CreateDraftCertificateResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.CreateDraftCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.EnhetType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonalType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.PatientType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.UtlatandeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.HsaId;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.PersonId;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.UtlatandeTyp;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.inera.ifv.hsawsresponder.v3.MiuInformationType;
import se.inera.webcert.hsa.services.HsaPersonService;
import se.inera.webcert.integration.builder.CreateNewDraftRequestBuilder;
import se.inera.webcert.integration.registry.IntegreradeEnheterRegistry;
import se.inera.webcert.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.webcert.integration.validator.CreateDraftCertificateValidator;
import se.inera.webcert.integration.validator.ValidationResult;
import se.inera.webcert.service.draft.IntygDraftService;
import se.inera.webcert.service.draft.dto.CreateNewDraftRequest;
import se.inera.webcert.service.dto.Vardenhet;
import se.inera.webcert.service.dto.Vardgivare;

@RunWith(MockitoJUnitRunner.class)
public class CreateDraftCertificateResponderTest {

	private static final String LOGICAL_ADDR = "1234567890";
	private static final String USER_HSAID = "SE1234567890";
	private static final String UNIT_HSAID = "SE0987654321";
	private static final String CAREGIVER_HSAID = "SE0000112233";
	private static final String UTKAST_ID = "abc123";

	@Mock
	IntygDraftService intygsUtkastService;
	
	@Mock
	HsaPersonService hsaPersonService;
	
	@Mock
	CreateNewDraftRequestBuilder mockRequestBuilder;
	
	@Mock
	CreateDraftCertificateValidator mockValidator;
	
	@Mock
	IntegreradeEnheterRegistry mockIntegreradeEnheterService;
	
	@InjectMocks
	CreateDraftCertificateResponderImpl responder;
	
	@Test
	public void test() {
		
	    ValidationResult validationResults = new ValidationResult();
        when(mockValidator.validate(any(UtlatandeType.class))).thenReturn(validationResults);
	    
		List<MiuInformationType> miuList = Arrays.asList(createMIU(USER_HSAID, UNIT_HSAID, LocalDateTime.now().plusYears(2)));
		when(hsaPersonService.checkIfPersonHasMIUsOnUnit(USER_HSAID, UNIT_HSAID)).thenReturn(miuList);
		
		Vardgivare vardgivare = new Vardgivare();
		vardgivare.setHsaId("SE1234567890-2B01");
		vardgivare.setNamn("Vardgivaren");
		
		Vardenhet vardenhet = new Vardenhet();
        vardenhet.setHsaId("SE1234567890-1A01");
        vardenhet.setNamn("Vardenheten");
		vardenhet.setVardgivare(vardgivare);
		
		CreateNewDraftRequest draftRequest = new CreateNewDraftRequest();
        draftRequest.setIntygId(UTKAST_ID);
        draftRequest.setVardenhet(vardenhet);
		
		when(mockRequestBuilder.buildCreateNewDraftRequest(any(UtlatandeType.class), any(MiuInformationType.class))).thenReturn(draftRequest);
		
		when(intygsUtkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(UTKAST_ID);
		
		when(mockIntegreradeEnheterService.addIfNotExistsIntegreradEnhet(any(IntegreradEnhetEntry.class))).thenReturn(Boolean.TRUE);
				
		CreateDraftCertificateType parameters = createParams();
		CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, parameters);
		
		assertNotNull(response);
		assertEquals(response.getResult().getResultCode(), ResultCodeType.OK);
		assertEquals(response.getUtlatandeId().getExtension(), UTKAST_ID);
		
		verify(intygsUtkastService).createNewDraft(any(CreateNewDraftRequest.class));
	}

	private CreateDraftCertificateType createParams() {
		
		UtlatandeType utlatande = new UtlatandeType();
		
		// Type
		UtlatandeTyp utlTyp = new UtlatandeTyp();
		utlTyp.setCode("fk7263");
		utlatande.setTypAvUtlatande(utlTyp);
		
		// HoSPerson
		HosPersonalType hosPerson = new HosPersonalType();
		hosPerson.setFullstandigtNamn("Abel Baker");
		
		HsaId userHsaId = new HsaId();
		userHsaId.setExtension(USER_HSAID);
		userHsaId.setRoot("USERHSAID");
		hosPerson.setPersonalId(userHsaId);
		
		EnhetType hosEnhet = new EnhetType();
		HsaId unitHsaId = new HsaId();
		unitHsaId.setExtension(UNIT_HSAID);
		unitHsaId.setRoot("UNITHSAID");
		hosEnhet.setEnhetsId(unitHsaId);
		hosPerson.setEnhet(hosEnhet);
			
		utlatande.setSkapadAv(hosPerson);
				
		// Patient
		PersonId personId = new PersonId();
		personId.setRoot("PERSNR");
		personId.setExtension("19121212-1212");
		
		PatientType patType = new PatientType();
		patType.setPersonId(personId);
		patType.getFornamn().add("Adam");
		patType.getFornamn().add("Bertil");
		patType.getMellannamn().add("Cesarsson");
		patType.getMellannamn().add("Davidsson");
		patType.setEfternamn("Eriksson");
		utlatande.setPatient(patType);
				
		CreateDraftCertificateType parameters = new CreateDraftCertificateType();
		parameters.setUtlatande(utlatande);
		
		return parameters;
	}
	
	private MiuInformationType createMIU(String personHsaId, String unitHsaId, LocalDateTime miuEndDate) {
		MiuInformationType miu = new MiuInformationType();
		miu.setCareGiver(CAREGIVER_HSAID);
		miu.setCareGiverName("Landstinget");
		miu.setCareUnitName("Sjukhuset");
		miu.setCareUnitHsaIdentity(unitHsaId);
		miu.setCareUnitEndDate(miuEndDate);
		miu.setHsaIdentityPerson(personHsaId);
		return miu;
	}
}
