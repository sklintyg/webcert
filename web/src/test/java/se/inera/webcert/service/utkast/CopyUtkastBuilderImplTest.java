package se.inera.webcert.service.utkast;

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
import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.CreateDraftCopyHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.dto.ValidateDraftResponse;
import se.inera.certificate.modules.support.api.dto.ValidationMessage;
import se.inera.certificate.modules.support.api.dto.ValidationStatus;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.pu.model.Person;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.intyg.dto.IntygContentHolder;
import se.inera.webcert.service.intyg.dto.IntygStatus;
import se.inera.webcert.service.intyg.dto.StatusType;
import se.inera.webcert.service.utkast.dto.CreateNewDraftCopyRequest;
import se.inera.webcert.service.utkast.util.CreateIntygsIdStrategy;

@RunWith(MockitoJUnitRunner.class)
public class CopyUtkastBuilderImplTest {

    private static final String INTYG_ID = "abc123";
    private static final String INTYG_COPY_ID = "def456";

    private static final String INTYG_JSON = "A bit of text representing json";

    private static final String INTYG_TYPE = "fk7263";

    private static final String PATIENT_SSN = "19121212-1212";
    private static final String PATIENT_FNAME = "Adam";
    private static final String PATIENT_MNAME = "Bertil";
    private static final String PATIENT_LNAME = "Caesarsson";

    private static final String PATIENT_NEW_SSN = "19121212-1414";

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

    private se.inera.webcert.service.dto.Vardenhet vardenhet;

    @InjectMocks
    private CopyUtkastBuilderImpl copyBuilder = new CopyUtkastBuilderImpl();

    @Before
    public void setup() {
        hoSPerson = new HoSPerson();
        hoSPerson.setHsaId("AAA");
        hoSPerson.setNamn("Dr Dengroth");
        hoSPerson.setBefattning("Befattning");
        hoSPerson.getSpecialiseringar().add("Ortoped");

        se.inera.webcert.service.dto.Vardgivare vardgivare = new se.inera.webcert.service.dto.Vardgivare();
        vardgivare.setHsaId("SE234234");
        vardgivare.setNamn("Vårdgivaren");

        vardenhet = new se.inera.webcert.service.dto.Vardenhet();
        vardenhet.setArbetsplatskod("00000");
        vardenhet.setNamn("Vårdenheten");
        vardenhet.setHsaId("SE234897348");
        vardenhet.setPostadress("Sjukvägen 1");
        vardenhet.setPostnummer("12345");
        vardenhet.setNamn("Testberga");
        vardenhet.setTelefonnummer("0123-456789");
        vardenhet.setEpost("ingen@ingen.se");
        vardenhet.setVardgivare(vardgivare);

        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hoSPerson.getHsaId());
        vardperson.setNamn(hoSPerson.getNamn());
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
        Person patientDetails = new Person(PATIENT_SSN, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345", "postort");

        InternalModelResponse imr = new InternalModelResponse(INTYG_JSON);
        when(mockModuleApi.createNewInternalFromTemplate(any(CreateDraftCopyHolder.class), any(InternalModelHolder.class))).thenReturn(imr);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<ValidationMessage>());
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(vdr);

        Utkast utkast = copyBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails);

        assertNotNull(utkast);
        assertNotNull(utkast.getModel());
        assertEquals(INTYG_TYPE, utkast.getIntygsTyp());
        assertEquals(PATIENT_SSN, utkast.getPatientPersonnummer());
        assertEquals(PATIENT_FNAME, utkast.getPatientFornamn());
        assertEquals(PATIENT_MNAME, utkast.getPatientMellannamn());
        assertEquals(PATIENT_LNAME, utkast.getPatientEfternamn());

    }

    @Test
    public void testPopulateCopyUtkastFromSignedIntygWithNoPatientDetails() throws Exception {

        IntygContentHolder ich = createIntygContentHolder();
        when(mockIntygService.fetchIntygData(INTYG_ID, INTYG_TYPE)).thenReturn(ich);

        CreateNewDraftCopyRequest copyRequest = buildCopyRequest();
        Person patientDetails = null;

        InternalModelResponse imr = new InternalModelResponse(INTYG_JSON);
        when(mockModuleApi.createNewInternalFromTemplate(any(CreateDraftCopyHolder.class), any(InternalModelHolder.class))).thenReturn(imr);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<ValidationMessage>());
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(vdr);

        Utkast utkast = copyBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails);

        assertNotNull(utkast);
        assertNotNull(utkast.getModel());
        assertEquals(INTYG_TYPE, utkast.getIntygsTyp());
        assertEquals(PATIENT_SSN, utkast.getPatientPersonnummer());
        assertEquals("Test", utkast.getPatientFornamn());
        assertNull(utkast.getPatientMellannamn());
        assertEquals("Testorsson", utkast.getPatientEfternamn());
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
        CreateNewDraftCopyRequest req = new CreateNewDraftCopyRequest();
        req.setOriginalIntygId(INTYG_ID);
        req.setTyp(INTYG_TYPE);
        req.setHosPerson(hoSPerson);
        req.setVardenhet(vardenhet);
        req.setPatientPersonnummer(PATIENT_SSN);
        return req;
    }

    private IntygContentHolder createIntygContentHolder() throws Exception {
        List<IntygStatus> status = new ArrayList<IntygStatus>();
        status.add(new IntygStatus(StatusType.RECEIVED, "MI", LocalDateTime.now()));
        status.add(new IntygStatus(StatusType.SENT, "FK", LocalDateTime.now()));
        Utlatande utlatande = new CustomObjectMapper().readValue(new ClassPathResource(
                "IntygDraftServiceImplTest/utlatande.json").getFile(), Utlatande.class);
        IntygContentHolder ich = new IntygContentHolder("<external-json/>", utlatande, status, false);
        return ich;
    }

}
