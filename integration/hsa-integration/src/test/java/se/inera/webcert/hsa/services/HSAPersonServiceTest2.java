package se.inera.webcert.hsa.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.ifv.hsawsresponder.v3.GetMiuForPersonResponseType;
import se.inera.ifv.hsawsresponder.v3.GetMiuForPersonType;
import se.inera.ifv.hsawsresponder.v3.MiuInformationType;
import se.inera.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;
import se.inera.webcert.hsa.stub.Medarbetaruppdrag;

@RunWith(MockitoJUnitRunner.class)
public class HSAPersonServiceTest2 {

    private static final String HSA_PERSON_ID = "SE11837399";

    private static final String HSA_UNIT_ID = "SE405900000";

    @Mock
    private HSAWebServiceCalls hsaWebServiceCalls;

    @InjectMocks
    private HsaPersonServiceImpl hsaPersonService;

    @Test
    public void testWithNoMius() {

        GetMiuForPersonResponseType miuResponse = new GetMiuForPersonResponseType();

        when(hsaWebServiceCalls.callMiuRights(any(GetMiuForPersonType.class))).thenReturn(miuResponse);

        List<MiuInformationType> res = hsaPersonService.checkIfPersonHasMIUsOnUnit(HSA_PERSON_ID, HSA_UNIT_ID);

        assertNotNull(res);
        assertEquals(0, res.size());
    }

    @Test
    public void testWithOneMiu() {

        GetMiuForPersonResponseType miuResponse = new GetMiuForPersonResponseType();
        MiuInformationType miu1 = new MiuInformationType();
        miu1.setHsaIdentity("001");
        miu1.setCareUnitHsaIdentity(HSA_UNIT_ID);
        miu1.setMiuPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu1.setCareUnitEndDate(LocalDateTime.now().plusYears(1));
        miuResponse.getMiuInformation().add(miu1);

        when(hsaWebServiceCalls.callMiuRights(any(GetMiuForPersonType.class))).thenReturn(miuResponse);

        List<MiuInformationType> res = hsaPersonService.checkIfPersonHasMIUsOnUnit(HSA_PERSON_ID, HSA_UNIT_ID);

        assertNotNull(res);
        assertEquals(1, res.size());
    }

    @Test
    public void testWithOneMiuNoMatch() {

        GetMiuForPersonResponseType miuResponse = new GetMiuForPersonResponseType();
        MiuInformationType miu1 = new MiuInformationType();
        miu1.setHsaIdentity("001");
        miu1.setCareUnitHsaIdentity("SE405900001");
        miu1.setMiuPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu1.setCareUnitEndDate(LocalDateTime.now().plusYears(1));
        miuResponse.getMiuInformation().add(miu1);

        when(hsaWebServiceCalls.callMiuRights(any(GetMiuForPersonType.class))).thenReturn(miuResponse);

        List<MiuInformationType> res = hsaPersonService.checkIfPersonHasMIUsOnUnit(HSA_PERSON_ID, HSA_UNIT_ID);

        assertNotNull(res);
        assertEquals(0, res.size());
    }

    @Test
    public void testWithSeveralMius() {

        GetMiuForPersonResponseType miuResponse = new GetMiuForPersonResponseType();
        MiuInformationType miu1 = new MiuInformationType();
        miu1.setHsaIdentity("001");
        miu1.setCareUnitHsaIdentity(HSA_UNIT_ID);
        miu1.setMiuPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu1.setCareUnitEndDate(LocalDateTime.now().plusYears(1));
        miuResponse.getMiuInformation().add(miu1);

        // this MIU expired 10 minutes ago
        MiuInformationType miu2 = new MiuInformationType();
        miu2.setHsaIdentity("002");
        miu2.setCareUnitHsaIdentity(HSA_UNIT_ID);
        miu2.setMiuPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu2.setCareUnitEndDate(LocalDateTime.now().minusMinutes(10));
        miuResponse.getMiuInformation().add(miu2);

        MiuInformationType miu3 = new MiuInformationType();
        miu3.setHsaIdentity("003");
        miu3.setCareUnitHsaIdentity("SE405900003");
        miu3.setMiuPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu3.setCareUnitEndDate(LocalDateTime.now().plusYears(1));
        miuResponse.getMiuInformation().add(miu3);

        when(hsaWebServiceCalls.callMiuRights(any(GetMiuForPersonType.class))).thenReturn(miuResponse);

        List<MiuInformationType> res = hsaPersonService.checkIfPersonHasMIUsOnUnit(HSA_PERSON_ID, HSA_UNIT_ID);

        assertNotNull(res);
        assertEquals(1, res.size());
    }

    @Test
    public void testWithSeveralMiusNoMatch() {

        GetMiuForPersonResponseType miuResponse = new GetMiuForPersonResponseType();
        MiuInformationType miu1 = new MiuInformationType();
        miu1.setHsaIdentity("001");
        miu1.setCareUnitHsaIdentity("SE405900001");
        miu1.setMiuPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu1.setCareUnitEndDate(LocalDateTime.now().plusYears(1));
        miuResponse.getMiuInformation().add(miu1);

        MiuInformationType miu2 = new MiuInformationType();
        miu2.setHsaIdentity("002");
        miu2.setCareUnitHsaIdentity("SE405900002");
        miu2.setMiuPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu2.setCareUnitEndDate(LocalDateTime.now().plusYears(1));
        miuResponse.getMiuInformation().add(miu2);

        MiuInformationType miu3 = new MiuInformationType();
        miu3.setHsaIdentity("003");
        miu3.setCareUnitHsaIdentity("SE405900003");
        miu3.setMiuPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu3.setCareUnitEndDate(LocalDateTime.now().plusYears(1));
        miuResponse.getMiuInformation().add(miu3);

        when(hsaWebServiceCalls.callMiuRights(any(GetMiuForPersonType.class))).thenReturn(miuResponse);

        List<MiuInformationType> res = hsaPersonService.checkIfPersonHasMIUsOnUnit(HSA_PERSON_ID, HSA_UNIT_ID);

        assertNotNull(res);
        assertEquals(0, res.size());
    }
}
