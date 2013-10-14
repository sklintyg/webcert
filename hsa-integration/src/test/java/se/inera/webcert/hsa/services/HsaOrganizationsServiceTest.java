package se.inera.webcert.hsa.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.ifv.hsawsresponder.v3.GetMiuForPersonResponseType;
import se.inera.ifv.hsawsresponder.v3.GetMiuForPersonType;
import se.inera.ifv.hsawsresponder.v3.MiuInformationType;
import se.inera.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;
import se.inera.webcert.hsa.model.Vardgivare;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class HsaOrganizationsServiceTest {

    private static final String HSA_ID = "hsaId";

    private static List<MiuInformationType> twoVardgivareWithTwoEnheter = new ArrayList<MiuInformationType>() {
        {
            add(miuInformation("N1", "Gul-1", "N", "Norr", "Vård och behandling"));
            add(miuInformation("N2", "Gul-2", "N", "Norr", "Vård och behandling"));
            add(miuInformation("S1", "Röd-1", "S", "Södra", "Vård och behandling"));
            add(miuInformation("S2", "Röd-2", "S", "Södra", "Vård och behandling"));
        }
    };

    private static List<MiuInformationType> vardenhetWithTwoDifferentMedarbetaruppdrag = new ArrayList<MiuInformationType>() {
        {
            add(miuInformation("N1", "Gul-1", "N", "Norr", "Vård och behandling"));
            add(miuInformation("N2", "Gul-2", "N", "Norr", "Medarbetarunderhållning"));
        }
    };

    @Mock
    private HSAWebServiceCalls client;

    @InjectMocks
    private HsaOrganizationsServiceImpl service;

    private GetMiuForPersonType hsaRequest() {
        GetMiuForPersonType request = new GetMiuForPersonType();
        request.setHsaIdentity(HSA_ID);
        return request;
    }

    @Test
    public void testEmptyResultSet() {
        when(client.callMiuRights(hsaRequest())).thenReturn(new GetMiuForPersonResponseType());

        Collection<Vardgivare> vardgivare = service.getAuthorizedEnheterForHosPerson(HSA_ID);
        assertTrue(vardgivare.isEmpty());
    }

    @Test
    public void testMultipleEnheterAndVardgivare() {
        GetMiuForPersonResponseType response = new GetMiuForPersonResponseType();
        response.getMiuInformation().addAll(twoVardgivareWithTwoEnheter);
        when(client.callMiuRights(hsaRequest())).thenReturn(response);

        List<Vardgivare> vardgivare = service.getAuthorizedEnheterForHosPerson(HSA_ID);
        assertEquals(2, vardgivare.size());

        Vardgivare norr = vardgivare.get(0);
        assertEquals("N", norr.getId());
        assertEquals("Norr", norr.getNamn());
        assertEquals(2, norr.getVardenheter().size());
        assertEquals("N1", norr.getVardenheter().get(0).getId());
        assertEquals("N2", norr.getVardenheter().get(1).getId());

        Vardgivare sodra = vardgivare.get(1);
        assertEquals("S", sodra.getId());
        assertEquals("Södra", sodra.getNamn());
        assertEquals(2, sodra.getVardenheter().size());
        assertEquals("S1", sodra.getVardenheter().get(0).getId());
        assertEquals("S2", sodra.getVardenheter().get(1).getId());

    }

    private static MiuInformationType miuInformation(String enhetId, String enhetNamn, String vardgivareId,
            String vardgivareNamn, String syfte) {
        MiuInformationType miuInformationType = new MiuInformationType();
        miuInformationType.setCareUnitHsaIdentity(enhetId);
        miuInformationType.setCareUnitName(enhetNamn);
        miuInformationType.setCareGiver(vardgivareId);
        miuInformationType.setCareGiverName(vardgivareNamn);
        miuInformationType.setMiuPurpose(syfte);
        return miuInformationType;
    }

    @Test
    public void testMedarbetaruppdragFiltering() {

        GetMiuForPersonResponseType response = new GetMiuForPersonResponseType();
        response.getMiuInformation().addAll(vardenhetWithTwoDifferentMedarbetaruppdrag);
        when(client.callMiuRights(hsaRequest())).thenReturn(response);

        List<Vardgivare> vardgivare = service.getAuthorizedEnheterForHosPerson(HSA_ID);

        assertEquals(1, vardgivare.size());

        Vardgivare norr = vardgivare.get(0);
        assertEquals(1, norr.getVardenheter().size());
        assertEquals("N1", norr.getVardenheter().get(0).getId());
    }
}
