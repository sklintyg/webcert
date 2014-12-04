package se.inera.webcert.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import se.inera.webcert.notifications.message.v1.HandelseType;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.service.draft.IntygDraftService;
import se.inera.webcert.service.draft.dto.CreateNewDraftRequest;
import se.inera.webcert.service.dto.Vardenhet;
import se.inera.webcert.service.dto.Vardgivare;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.test.TestIntygFactory;

@RunWith(MockitoJUnitRunner.class)
public class CreateDraftCertificateResponderTest {

	private static final String LOGICAL_ADDR = "1234567890";
	private static final String USER_HSAID = "SE1234567890";
	private static final String UNIT_HSAID = "SE0987654321";
	private static final String CAREGIVER_HSAID = "SE0000112233";
	private static final String UTKAST_ID = "abc123";

	@Mock
	IntygDraftService mockIntygsUtkastService;
	
	@Mock
	HsaPersonService mockHsaPersonService;
	
	@Mock
	CreateNewDraftRequestBuilder mockRequestBuilder;
	
	@Mock
	CreateDraftCertificateValidator mockValidator;
	
	@Mock
	IntegreradeEnheterRegistry mockIntegreradeEnheterService;

    @Mock
    NotificationService mockNotificationService;

	@InjectMocks
	CreateDraftCertificateResponderImpl responder;

    /**
     * When a new certificate draft is being created the caller
     * should get a success response returned and any stakeholder
     * should be notified with a notification message:
     */
	@Test
	public void whenNewCertificateDraftSuccessResponse() {
		
	    ValidationResult validationResults = new ValidationResult();
        when(mockValidator.validate(any(UtlatandeType.class))).thenReturn(validationResults);
	    
		List<MiuInformationType> miuList = Arrays.asList(createMIU(USER_HSAID, UNIT_HSAID, LocalDateTime.now().plusYears(2)));
		when(mockHsaPersonService.checkIfPersonHasMIUsOnUnit(USER_HSAID, UNIT_HSAID)).thenReturn(miuList);
		
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
		when(mockIntygsUtkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(UTKAST_ID);
        when(mockIntygsUtkastService.getDraft(anyString())).thenReturn(TestIntygFactory.createIntyg(UTKAST_ID, LocalDateTime.now()));
        when(mockIntegreradeEnheterService.addIfNotExistsIntegreradEnhet(any(IntegreradEnhetEntry.class))).thenReturn(Boolean.TRUE);

        ArgumentCaptor<NotificationRequestType> notificationRequestTypeArgumentCaptor = ArgumentCaptor.forClass(NotificationRequestType.class);

		CreateDraftCertificateType parameters = createParams();
		CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, parameters);

        verify(mockIntygsUtkastService).createNewDraft(any(CreateNewDraftRequest.class));
        verify(mockIntygsUtkastService).getDraft(anyString());
        verify(mockNotificationService).notify(notificationRequestTypeArgumentCaptor.capture());

        // Assert response content
        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.OK);
        assertEquals(response.getUtlatandeId().getExtension(), UTKAST_ID);

        // Assert notification message
        NotificationRequestType notificationRequestType = notificationRequestTypeArgumentCaptor.getValue();
        assertEquals(UTKAST_ID, notificationRequestType.getIntygsId());
        assertEquals(HandelseType.INTYGSUTKAST_SKAPAT, notificationRequestType.getHandelse());

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
