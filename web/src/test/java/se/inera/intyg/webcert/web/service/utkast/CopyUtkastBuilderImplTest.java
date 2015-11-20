package se.inera.intyg.webcert.web.service.utkast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.Status;
import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.CreateDraftCopyHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.dto.Personnummer;
import se.inera.certificate.modules.support.api.dto.ValidateDraftResponse;
import se.inera.certificate.modules.support.api.dto.ValidationMessage;
import se.inera.certificate.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.webcert.web.service.dto.Vardenhet;
import se.inera.intyg.webcert.web.service.dto.Vardgivare;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.pu.model.Person;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.utkast.dto.CopyUtkastBuilderResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.util.CreateIntygsIdStrategy;

@RunWith(MockitoJUnitRunner.class)
public class CopyUtkastBuilderImplTest {

    private static final String INTYG_ID = "abc123";
    private static final String INTYG_COPY_ID = "def456";

    private static final String INTYG_JSON = "A bit of text representing json";

    private static final String INTYG_TYPE = "fk7263";

    private static final Personnummer PATIENT_SSN = new Personnummer("19121212-1212");
    private static final String PATIENT_FNAME = "Adam";
    private static final String PATIENT_MNAME = "Bertil";
    private static final String PATIENT_LNAME = "Caesarsson";

    private static final Personnummer PATIENT_NEW_SSN = new Personnummer("19121212-1414");

    private static final String VARDENHET_ID = "SE00001234-5678";
    private static final String VARDENHET_NAME = "Vårdenheten 1";

    private static final String VARDGIVARE_ID = "SE00001234-1234";
    private static final String VARDGIVARE_NAME = "Vårdgivaren 1";

    private static final String HOSPERSON_ID = "SE12345678-0001";
    private static final String HOSPERSON_NAME = "Dr Börje Dengroth";

    @Mock
    private IntygService mockIntygService;

    @Mock
    private UtkastRepository mockUtkastRepository;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Spy
    private CreateIntygsIdStrategy mockIdStrategy = new CreateIntygsIdStrategy() {
        @Override
        public String createId() {
            return INTYG_COPY_ID;
        }
    };

    private ModuleApi mockModuleApi;

    private HoSPerson hoSPerson;

    private Vardenhet vardenhet;

    @InjectMocks
    private CopyUtkastBuilderImpl copyBuilder = new CopyUtkastBuilderImpl();

    @Before
    public void setup() {
        hoSPerson = new HoSPerson();
        hoSPerson.setHsaId(HOSPERSON_ID);
        hoSPerson.setNamn(HOSPERSON_NAME);

        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setHsaId(VARDGIVARE_ID);
        vardgivare.setNamn(VARDGIVARE_NAME);

        vardenhet = new Vardenhet();
        vardenhet.setHsaId(VARDENHET_ID);
        vardenhet.setNamn(VARDENHET_NAME);
        vardenhet.setVardgivare(vardgivare);
    }

    @Before
    public void expectCallToModuleRegistry() throws Exception {
        this.mockModuleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
    }

    @Test
    public void testPopulateCopyUtkastFromSignedIntyg() throws Exception {

        IntygContentHolder ich = createIntygContentHolder();
        when(mockIntygService.fetchIntygData(INTYG_ID, INTYG_TYPE)).thenReturn(ich);

        CreateNewDraftCopyRequest copyRequest = buildCopyRequest();
        Person patientDetails = new Person(PATIENT_SSN, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345", "postort");

        InternalModelResponse imr = new InternalModelResponse(INTYG_JSON);
        when(mockModuleApi.createNewInternalFromTemplate(any(CreateDraftCopyHolder.class), any(InternalModelHolder.class))).thenReturn(imr);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<ValidationMessage>());
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(vdr);

        CopyUtkastBuilderResponse builderResponse = copyBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails);

        assertNotNull(builderResponse.getUtkastCopy());
        assertNotNull(builderResponse.getUtkastCopy().getModel());
        assertEquals(INTYG_TYPE, builderResponse.getUtkastCopy().getIntygsTyp());
        assertEquals(PATIENT_SSN, builderResponse.getUtkastCopy().getPatientPersonnummer());
        assertEquals(PATIENT_FNAME, builderResponse.getUtkastCopy().getPatientFornamn());
        assertEquals(PATIENT_MNAME, builderResponse.getUtkastCopy().getPatientMellannamn());
        assertEquals(PATIENT_LNAME, builderResponse.getUtkastCopy().getPatientEfternamn());

    }

    @Test
    public void testPopulateCopyUtkastFromOriginal() throws Exception {

        Utkast orgUtkast = createOriginalUtkast();
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(orgUtkast);

        CreateNewDraftCopyRequest copyRequest = buildCopyRequest();
        Person patientDetails = new Person(PATIENT_SSN, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345", "postort");

        InternalModelResponse imr = new InternalModelResponse(INTYG_JSON);
        when(mockModuleApi.createNewInternalFromTemplate(any(CreateDraftCopyHolder.class), any(InternalModelHolder.class))).thenReturn(imr);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<ValidationMessage>());
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(vdr);

        CopyUtkastBuilderResponse builderResponse = copyBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails);

        assertNotNull(builderResponse.getUtkastCopy());
        assertNotNull(builderResponse.getUtkastCopy().getModel());
        assertEquals(INTYG_TYPE, builderResponse.getUtkastCopy().getIntygsTyp());
        assertEquals(PATIENT_SSN, builderResponse.getUtkastCopy().getPatientPersonnummer());
        assertEquals(PATIENT_FNAME, builderResponse.getUtkastCopy().getPatientFornamn());
        assertNotNull(builderResponse.getUtkastCopy().getPatientMellannamn());
        assertEquals(PATIENT_LNAME, builderResponse.getUtkastCopy().getPatientEfternamn());
    }

    @Test
    public void testPopulateCopyUtkastFromOriginalWhenIntegratedAndWithUpdatedSSN() throws Exception {

        Utkast orgUtkast = createOriginalUtkast();
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(orgUtkast);

        CreateNewDraftCopyRequest copyRequest = buildCopyRequest();
        copyRequest.setNyttPatientPersonnummer(PATIENT_NEW_SSN);
        copyRequest.setDjupintegrerad(true);

        InternalModelResponse imr = new InternalModelResponse(INTYG_JSON);
        when(mockModuleApi.createNewInternalFromTemplate(any(CreateDraftCopyHolder.class), any(InternalModelHolder.class))).thenReturn(imr);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<ValidationMessage>());
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(vdr);

        CopyUtkastBuilderResponse builderResponse = copyBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, null);

        assertNotNull(builderResponse.getUtkastCopy());
        assertNotNull(builderResponse.getUtkastCopy().getModel());
        assertEquals(INTYG_TYPE, builderResponse.getUtkastCopy().getIntygsTyp());
        assertEquals(PATIENT_NEW_SSN, builderResponse.getUtkastCopy().getPatientPersonnummer());
        assertEquals(PATIENT_FNAME, builderResponse.getUtkastCopy().getPatientFornamn());
        assertNotNull(builderResponse.getUtkastCopy().getPatientMellannamn());
        assertEquals(PATIENT_LNAME, builderResponse.getUtkastCopy().getPatientEfternamn());
    }

    @Test
    public void testPopulateCopyUtkastFromSignedIntygWithNoPatientDetails() throws Exception {

        IntygContentHolder ich = createIntygContentHolder();
        when(mockIntygService.fetchIntygData(INTYG_ID, INTYG_TYPE)).thenReturn(ich);

        CreateNewDraftCopyRequest copyRequest = buildCopyRequest();

        InternalModelResponse imr = new InternalModelResponse(INTYG_JSON);
        when(mockModuleApi.createNewInternalFromTemplate(any(CreateDraftCopyHolder.class), any(InternalModelHolder.class))).thenReturn(imr);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<ValidationMessage>());
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(vdr);

        CopyUtkastBuilderResponse builderResponse = copyBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, null);

        assertNotNull(builderResponse.getUtkastCopy());
        assertNotNull(builderResponse.getUtkastCopy().getModel());
        assertEquals(INTYG_TYPE, builderResponse.getUtkastCopy().getIntygsTyp());
        assertEquals(PATIENT_SSN, builderResponse.getUtkastCopy().getPatientPersonnummer());
        assertEquals("Test", builderResponse.getUtkastCopy().getPatientFornamn());
        assertNull(builderResponse.getUtkastCopy().getPatientMellannamn());
        assertEquals("Testorsson", builderResponse.getUtkastCopy().getPatientEfternamn());
    }

    @Test
    public void testExtractNamePartsFromFullName() {

        String[] res = copyBuilder.extractNamePartsFromFullName(null);
        assertNotNull(res);

        res = copyBuilder.extractNamePartsFromFullName("");
        assertNotNull(res);

        res = copyBuilder.extractNamePartsFromFullName("  ");
        assertNotNull(res);
        assertEquals("", res[0]);
        assertEquals("", res[1]);

        res = copyBuilder.extractNamePartsFromFullName("Adam");
        assertNotNull(res);
        assertEquals("Adam", res[0]);
        assertEquals("", res[1]);

        res = copyBuilder.extractNamePartsFromFullName("Adam Caesarsson");
        assertNotNull(res);
        assertEquals("Adam", res[0]);
        assertEquals("Caesarsson", res[1]);

        res = copyBuilder.extractNamePartsFromFullName("Adam Bertil Caesarsson");
        assertNotNull(res);
        assertEquals("Adam Bertil", res[0]);
        assertEquals("Caesarsson", res[1]);
    }

    private CreateNewDraftCopyRequest buildCopyRequest() {
        return new CreateNewDraftCopyRequest(INTYG_ID, INTYG_TYPE, PATIENT_SSN, hoSPerson, vardenhet);
    }

    private IntygContentHolder createIntygContentHolder() throws Exception {
        List<Status> status = new ArrayList<>();
        status.add(new Status(CertificateState.RECEIVED, "MI", LocalDateTime.now()));
        status.add(new Status(CertificateState.SENT, "FK", LocalDateTime.now()));
        Utlatande utlatande = new CustomObjectMapper().readValue(new ClassPathResource(
                "IntygDraftServiceImplTest/utlatande.json").getFile(), Utlatande.class);
        return new IntygContentHolder("<external-json/>", utlatande, status, false);
    }

    private Utkast createOriginalUtkast() {

        Utkast orgUtkast = new Utkast();
        orgUtkast.setIntygsId(INTYG_COPY_ID);
        orgUtkast.setIntygsTyp(INTYG_TYPE);
        orgUtkast.setPatientPersonnummer(PATIENT_SSN);
        orgUtkast.setPatientFornamn(PATIENT_FNAME);
        orgUtkast.setPatientMellannamn(PATIENT_MNAME);
        orgUtkast.setPatientEfternamn(PATIENT_LNAME);
        orgUtkast.setEnhetsId(VARDENHET_ID);
        orgUtkast.setEnhetsNamn(VARDENHET_NAME);
        orgUtkast.setVardgivarId(VARDGIVARE_ID);
        orgUtkast.setVardgivarNamn(VARDGIVARE_NAME);
        orgUtkast.setModel(INTYG_JSON);

        VardpersonReferens vpRef = new VardpersonReferens();
        vpRef.setHsaId(HOSPERSON_ID);
        vpRef.setNamn(HOSPERSON_NAME);

        orgUtkast.setSenastSparadAv(vpRef);
        orgUtkast.setSkapadAv(vpRef);

        return orgUtkast;
    }

}
