package se.inera.intyg.webcert.integration.tak.service;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.Before;
import se.inera.intyg.infra.integration.hsa.exception.HsaServiceCallException;
import se.inera.intyg.infra.integration.hsa.services.HsaOrganizationsService;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.integration.tak.consumer.TakConsumerImpl;
import se.inera.intyg.webcert.integration.tak.model.TakLogicalAddress;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class TakServiceImplTest {

    private static final String NTJP_ID = "1";
    private static final String CONTRACT_ID = "2";
    private static final String HSAID_OK = "SE2321000198-016965";

    @Mock
    private HsaOrganizationsService hsa;

    @Mock
    private TakConsumerImpl consumer;

    @InjectMocks
    private TakServiceImpl impl;

    @Before
    public void setup() {
        impl = new TakServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    private TakLogicalAddress[] buildTakLogicalAddress() {
        TakLogicalAddress logicalAddress = new TakLogicalAddress();
        logicalAddress.setId("34");
        logicalAddress.setDescription("A description");
        logicalAddress.setLogicalAddress(HSAID_OK);
        return new TakLogicalAddress[] {logicalAddress};
    }

    @Test
    public void testHappyHappy() throws HsaServiceCallException {
        when(consumer.getConnectionPointId()).thenReturn(NTJP_ID);
        when(consumer.getServiceContractId(matches(NTJP_ID))).thenReturn(CONTRACT_ID);
        when(consumer.doLookup(anyString(), anyString(), anyString())).thenReturn(buildTakLogicalAddress());

        assertTrue(impl.verifyTakningForCareUnit("SE2321000198-016965", "test", "V1",
                new IntygUser("SE2321000198-016965")).isValid());
    }
}