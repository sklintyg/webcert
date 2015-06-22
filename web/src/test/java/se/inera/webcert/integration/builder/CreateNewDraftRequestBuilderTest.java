package se.inera.webcert.integration.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Patient;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.TypAvUtlatande;
import se.inera.ifv.hsawsresponder.v3.MiuInformationType;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.services.HsaOrganizationsService;
import se.inera.webcert.service.utkast.dto.CreateNewDraftRequest;

@RunWith(MockitoJUnitRunner.class)
public class CreateNewDraftRequestBuilderTest {

	private static final String CERT_TYPE = "fk7263";
	private static final String USER_HSAID = "SE1234567890";
	private static final String UNIT_HSAID = "SE0987654321";
	private static final String CAREGIVER_HSAID = "SE0000112233";

	@Mock
	HsaOrganizationsService orgServiceMock;

	@InjectMocks
	CreateNewDraftRequestBuilderImpl builder;

	@Test
	public void test() {
		
		Vardenhet hsaVardenhet = createHsaVardenhet();
		when(orgServiceMock.getVardenhet(anyString())).thenReturn(hsaVardenhet);
		
		Utlatande utlatande = createUtlatande();

		MiuInformationType miu = createMIU(USER_HSAID, UNIT_HSAID, LocalDateTime.now().plusYears(2));

		CreateNewDraftRequest res = builder.buildCreateNewDraftRequest(utlatande, miu);
		
		assertNotNull(res);
		
		assertEquals(CERT_TYPE, res.getIntygType());
		
		assertEquals(USER_HSAID, res.getHosPerson().getHsaId());
		assertNotNull(res.getHosPerson().getNamn());
		
		assertEquals(UNIT_HSAID, res.getVardenhet().getHsaId());
		assertNotNull(res.getVardenhet().getNamn());
		assertNotNull(res.getVardenhet().getArbetsplatskod());
		assertNotNull(res.getVardenhet().getTelefonnummer());
		assertNotNull(res.getVardenhet().getPostadress());
		assertNotNull(res.getVardenhet().getPostnummer());
		assertNotNull(res.getVardenhet().getPostort());
		
		assertEquals(CAREGIVER_HSAID, res.getVardenhet().getVardgivare().getHsaId());
		assertNotNull(res.getVardenhet().getVardgivare().getNamn());
		
		assertEquals("19121212-1212", res.getPatient().getPersonnummer());
		assertEquals("Adam Bertil", res.getPatient().getFornamn());
		assertEquals("Cesarsson Davidsson", res.getPatient().getMellannamn());
		assertEquals("Eriksson", res.getPatient().getEfternamn());	
		
	}

	private Vardenhet createHsaVardenhet() {
		
		Vardenhet hsaVardenhet = new Vardenhet();
		hsaVardenhet.setId(UNIT_HSAID);
		hsaVardenhet.setNamn("Vardenheten");
		hsaVardenhet.setArbetsplatskod("0000001");
		hsaVardenhet.setPostadress("Postaddr");
		hsaVardenhet.setPostnummer("12345");
		hsaVardenhet.setPostort("Staden");
		hsaVardenhet.setTelefonnummer("0123-456789");
				
		return hsaVardenhet;
	}

	private Utlatande createUtlatande() {

		Utlatande utlatande = new Utlatande();

		// Type
		TypAvUtlatande utlTyp = new TypAvUtlatande();
		utlTyp.setCode(CERT_TYPE);
		utlatande.setTypAvUtlatande(utlTyp);

		// HoSPerson
		HosPersonal hosPerson = new HosPersonal();
		hosPerson.setFullstandigtNamn("Abel Baker");

		HsaId userHsaId = new HsaId();
		userHsaId.setExtension(USER_HSAID);
		userHsaId.setRoot("USERHSAID");
		hosPerson.setPersonalId(userHsaId);

		Enhet hosEnhet = new Enhet();
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

		Patient patType = new Patient();
		patType.setPersonId(personId);
		patType.getFornamn().add("Adam");
		patType.getFornamn().add("Bertil");
		patType.getMellannamn().add("Cesarsson");
		patType.getMellannamn().add("Davidsson");
		patType.setEfternamn("Eriksson");
		utlatande.setPatient(patType);

		return utlatande;
	}

	private MiuInformationType createMIU(String personHsaId, String unitHsaId,
			LocalDateTime miuEndDate) {
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
