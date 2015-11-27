package se.inera.intyg.webcert.integration.fmb.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import se.inera.intyg.webcert.persistence.fmb.model.Fmb;
import se.inera.intyg.webcert.persistence.fmb.model.FmbCallType;
import se.inera.intyg.webcert.persistence.fmb.model.FmbType;
import se.inera.intyg.webcert.persistence.fmb.repository.FmbRepository;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getdiagnosinformationresponder.v1.GetDiagnosInformationResponderInterface;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getdiagnosinformationresponder.v1.GetDiagnosInformationResponseType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getdiagnosinformationresponder.v1.GetDiagnosInformationType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getfmbresponder.v1.GetFmbResponderInterface;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getfmbresponder.v1.GetFmbResponseType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getfmbresponder.v1.GetFmbType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getversionsresponder.v1.GetVersionsResponderInterface;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getversionsresponder.v1.GetVersionsResponseType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getversionsresponder.v1.GetVersionsType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.BeslutsunderlagType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.DiagnosInformationType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.HuvuddiagnosType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.ICD10SEType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.VersionType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.VersionerType;

public class FmbServiceImplTest {

    @InjectMocks
    private FmbServiceImpl fmbServiceImpl;

    @Mock
    private GetDiagnosInformationResponderInterface getDiagnosInformationResponder;

    @Mock
    private GetFmbResponderInterface getFmbResponder;

    @Mock
    private GetVersionsResponderInterface getVersionsResponder;

    @Mock
    private FmbRepository fmbRepository;

    @Captor
    private ArgumentCaptor<List<Fmb>> fmbCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testThatDotsInIcd10CodesAreRemoved() throws Exception {
        //Given
        setDiagnosInformationResponder("", createDiagnosInformationType("", "J22.4"));
        setupFmbResponse("", createBeslutsunderlag("", "J22.2"));
        setupVersionsMock("", "");

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(2)).save(fmbCaptor.capture());
        List<List<Fmb>> fmbCaptorAllValues = fmbCaptor.getAllValues();
        assertEquals("J224", fmbCaptorAllValues.get(0).get(0).getIcd10());
        assertEquals("J222", fmbCaptorAllValues.get(1).get(0).getIcd10());
    }

    @Test
    public void testUpdateDiagnosInfoUpdatesCorrectlyOnEmptyDb() throws Exception {
        //Given
        final String beskrivning = "test";
        setDiagnosInformationResponder("", createDiagnosInformationType(beskrivning, "J22"));
        setupFmbResponse("");
        setupVersionsMock("", "");

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(1)).deleteInBatch(fmbCaptor.capture());
        assertEquals(0, fmbCaptor.getValue().size());
        Mockito.verify(fmbRepository, times(1)).save(fmbCaptor.capture());
        assertEquals(1, fmbCaptor.getValue().size());
        assertEquals(beskrivning, findFmbType(FmbType.FALT4, fmbCaptor.getValue()).getText());
    }

    @Test
    public void testUpdateFmbInfoUpdatesCorrectlyOnEmptyDb() throws Exception {
        //Given
        final String underlag = "test";
        setDiagnosInformationResponder("");
        setupFmbResponse("", createBeslutsunderlag(underlag, "J22"));
        setupVersionsMock("", "");

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(1)).deleteInBatch(fmbCaptor.capture());
        assertEquals(0, fmbCaptor.getValue().size());
        Mockito.verify(fmbRepository, times(1)).save(fmbCaptor.capture());
        assertEquals(1, fmbCaptor.getValue().size());
        assertEquals(underlag, findFmbType(FmbType.FALT8B, fmbCaptor.getValue()).getText());
    }

    @Test
    public void testUpdateDiagnosInfoUpdatesCorrectlyOnNonEmptyDb() throws Exception {
        //Given
        final String beskrivning = "test";
        setDiagnosInformationResponder("", createDiagnosInformationType(beskrivning, "J22"));
        setupFmbResponse("");
        setupVersionsMock("", "");
        final List<Fmb> fmbs = Arrays.asList(createFmbDi("A10", "test10"), createFmbDi("A11", "test11"));
        Mockito.doReturn(fmbs).when(fmbRepository).findByUrsprung(FmbCallType.DIAGNOSINFORMATION);

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(1)).deleteInBatch(fmbCaptor.capture());
        assertEquals(fmbs, fmbCaptor.getValue());
        Mockito.verify(fmbRepository, times(1)).save(fmbCaptor.capture());
        assertEquals(1, fmbCaptor.getValue().size());
        assertEquals(beskrivning, findFmbType(FmbType.FALT4, fmbCaptor.getValue()).getText());
    }

    @Test
    public void testUpdateFmbInfoUpdatesCorrectlyOnNonEmptyDb() throws Exception {
        //Given
        final String underlag = "test";
        setDiagnosInformationResponder("");
        setupFmbResponse("", createBeslutsunderlag(underlag, "J22"));
        setupVersionsMock("", "");
        final List<Fmb> fmbs = Arrays.asList(createFmbFmb("A10", "test10"), createFmbFmb("A11", "test11"));
        Mockito.doReturn(fmbs).when(fmbRepository).findByUrsprung(FmbCallType.FMB);

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(1)).deleteInBatch(fmbCaptor.capture());
        assertEquals(fmbs, fmbCaptor.getValue());
        Mockito.verify(fmbRepository, times(1)).save(fmbCaptor.capture());
        assertEquals(1, fmbCaptor.getValue().size());
        assertEquals(underlag, findFmbType(FmbType.FALT8B, fmbCaptor.getValue()).getText());
    }

    @Test
    public void testUpdateDiagnosInfoFailsKeepsExistingData() throws Exception {
        //Given
        setupVersionsMock("", "");

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(0)).save(any(Iterable.class));
        Mockito.verify(fmbRepository, times(0)).deleteInBatch(any(Iterable.class));
    }

    @Test
    public void testBothAreUpdatedWhenVersionIsUpdatedForBoth() throws Exception {
        //Given
        setupVersionsMock("1", "2");
        final List<Fmb> fmbs = Arrays.asList(createFmbFmb("A10", "test10", "3"));
        Mockito.doReturn(fmbs).when(fmbRepository).findByUrsprung(FmbCallType.FMB);
        final List<Fmb> fmbsDi = Arrays.asList(createFmbDi("A11", "test11", "4"));
        Mockito.doReturn(fmbsDi).when(fmbRepository).findByUrsprung(FmbCallType.DIAGNOSINFORMATION);
        setDiagnosInformationResponder("", createDiagnosInformationType("", "J22"));
        setupFmbResponse("", createBeslutsunderlag("", "J22"));

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(2)).save(any(Iterable.class));
    }

    @Test
    public void testOnlyFmbIsUpdatedWhenVersionIsUpdatedForFmb() throws Exception {
        //Given
        setupVersionsMock("1", "2");
        final List<Fmb> fmbs = Arrays.asList(createFmbFmb("A10", "test10", "3"));
        Mockito.doReturn(fmbs).when(fmbRepository).findByUrsprung(FmbCallType.FMB);
        final List<Fmb> fmbsDi = Arrays.asList(createFmbDi("A11", "test11", "1"));
        Mockito.doReturn(fmbsDi).when(fmbRepository).findByUrsprung(FmbCallType.DIAGNOSINFORMATION);
        setDiagnosInformationResponder("", createDiagnosInformationType("", "J22"));
        setupFmbResponse("", createBeslutsunderlag("", "J22"));

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(1)).save(any(Iterable.class));
    }

    @Test
    public void testOnlyDiIsUpdatedWhenVersionIsUpdatedForDi() throws Exception {
        //Given
        setupVersionsMock("1", "2");
        final List<Fmb> fmbs = Arrays.asList(createFmbFmb("A10", "test10", "2"));
        Mockito.doReturn(fmbs).when(fmbRepository).findByUrsprung(FmbCallType.FMB);
        final List<Fmb> fmbsDi = Arrays.asList(createFmbDi("A11", "test11", "4"));
        Mockito.doReturn(fmbsDi).when(fmbRepository).findByUrsprung(FmbCallType.DIAGNOSINFORMATION);
        setDiagnosInformationResponder("", createDiagnosInformationType("", "J22"));
        setupFmbResponse("", createBeslutsunderlag("", "J22"));

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(1)).save(any(Iterable.class));
    }

    @Test
    public void testNoUpdatedWhenVersionIsNotChanged() throws Exception {
        //Given
        setupVersionsMock("1", "2");
        final List<Fmb> fmbs = Arrays.asList(createFmbFmb("A10", "test10", "2"));
        Mockito.doReturn(fmbs).when(fmbRepository).findByUrsprung(FmbCallType.FMB);
        final List<Fmb> fmbsDi = Arrays.asList(createFmbDi("A11", "test11", "1"));
        Mockito.doReturn(fmbsDi).when(fmbRepository).findByUrsprung(FmbCallType.DIAGNOSINFORMATION);
        setDiagnosInformationResponder("", createDiagnosInformationType("", "J22"));
        setupFmbResponse("", createBeslutsunderlag("", "J22"));

        //When
        fmbServiceImpl.updateData();

        //Then
        Mockito.verify(fmbRepository, times(0)).save(any(Iterable.class));
    }

    private Fmb createFmbDi(String icd10, String text) {
        return createFmbDi(icd10, text, "unknown");
    }

    private Fmb createFmbDi(String icd10, String text, String lastUpdate) {
        return new Fmb(icd10, FmbType.FALT4, FmbCallType.DIAGNOSINFORMATION, text, lastUpdate);
    }

    private Fmb createFmbFmb(String icd10, String text) {
        return createFmbFmb(icd10, text, "unknown");
    }

    private Fmb createFmbFmb(String icd10, String text, String lastUpdate) {
        return new Fmb(icd10, FmbType.FALT4, FmbCallType.FMB, text, lastUpdate);
    }

    private BeslutsunderlagType createBeslutsunderlag(String underlag, String... icd10Codes) {
        final BeslutsunderlagType beslutsunderlag = new BeslutsunderlagType();
        beslutsunderlag.getHuvuddiagnos().addAll(createHuvuddiagnoser(icd10Codes));
        beslutsunderlag.setTextuelltUnderlag(underlag);
        return beslutsunderlag;
    }

    private Fmb findFmbType(FmbType fmbType, List<Fmb> fmbs) {
        for (Fmb fmb : fmbs) {
            if (fmbType.equals(fmb.getTyp())) {
                return fmb;
            }
        }
        throw new RuntimeException("Could not find Fmb with type: " + fmbType);
    }

    private DiagnosInformationType createDiagnosInformationType(String funktionsnedsattningBeskrivning, String... icd10Codes) {
        final DiagnosInformationType dxInfo = new DiagnosInformationType();
        dxInfo.setFunktionsnedsattningBeskrivning(funktionsnedsattningBeskrivning);
        dxInfo.getHuvuddiagnos().addAll(createHuvuddiagnoser(icd10Codes));
        return dxInfo;
    }

    private List<HuvuddiagnosType> createHuvuddiagnoser(String[] icd10Codes) {
        final List<HuvuddiagnosType> huvuddiagnosTypes = new ArrayList<>();
        for (String icd10Code : icd10Codes) {
            final HuvuddiagnosType huvuddiagnos = new HuvuddiagnosType();
            final ICD10SEType kod = new ICD10SEType();
            kod.setCode(icd10Code);
            huvuddiagnos.setKod(kod);
            huvuddiagnosTypes.add(huvuddiagnos);
        }
        return huvuddiagnosTypes;
    }

    private GetDiagnosInformationResponseType setDiagnosInformationResponder(String senateAndring, DiagnosInformationType... diagnosInformations) {
        final GetDiagnosInformationResponseType response = new GetDiagnosInformationResponseType();
        final VersionType version = new VersionType();
        version.setSenateAndring(senateAndring);
        response.setVersion(version);
        for (DiagnosInformationType diagnosInformation : diagnosInformations) {
            response.getDiagnosInformation().add(diagnosInformation);
        }
        return Mockito.doReturn(response).when(getDiagnosInformationResponder).getDiagnosInformation(anyString(), any(GetDiagnosInformationType.class));
    }

    private void setupVersionsMock(String diagnosInformationSenateAndring, String fmbSenateAndring) {
        final GetVersionsResponseType response = new GetVersionsResponseType();
        final VersionerType versioner = new VersionerType();
        versioner.setDiagnosInformationSenateAndring(diagnosInformationSenateAndring);
        versioner.setFmbSenateAndring(fmbSenateAndring);
        response.setVersioner(versioner);
        Mockito.doReturn(response).when(getVersionsResponder).getVersions(anyString(), any(GetVersionsType.class));
    }

    private void setupFmbResponse(String senateAndring, BeslutsunderlagType... beslutsunderlags) {
        final GetFmbResponseType response = new GetFmbResponseType();
        final VersionType version = new VersionType();
        version.setSenateAndring(senateAndring);
        response.setVersion(version);
        for (BeslutsunderlagType beslutsunderlag : beslutsunderlags) {
            response.getBeslutsunderlag().add(beslutsunderlag);
        }
        Mockito.doReturn(response).when(getFmbResponder).getFmb(anyString(), any(GetFmbType.class));
    }

}
