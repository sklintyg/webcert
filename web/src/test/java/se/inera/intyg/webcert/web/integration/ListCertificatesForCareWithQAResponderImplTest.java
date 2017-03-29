package se.inera.intyg.webcert.web.integration;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotifications;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListCertificatesForCareWithQAResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListCertificatesForCareWithQAType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

@RunWith(MockitoJUnitRunner.class)
public class ListCertificatesForCareWithQAResponderImplTest {

    @Mock
    private IntygService intygService;

    @InjectMocks
    private ListCertificatesForCareWithQAResponderImpl responder;

    @Test
    public void testListCertificatesForCareWithQA() {
        final Personnummer personnummer = new Personnummer("191212121212");
        final String enhet = "enhetHsaId";
        Handelse handelse = new Handelse();
        handelse.setCode(HandelsekodEnum.SKAPAT);
        handelse.setTimestamp(LocalDateTime.now());

        when(intygService.listCertificatesForCareWithQA(eq(personnummer), eq(Arrays.asList(enhet)))).thenReturn(Arrays.asList(
                new IntygWithNotifications(null, Arrays.asList(handelse), new ArendeCount(1, 1, 1, 1), new ArendeCount(2, 2, 2, 2))));

        ListCertificatesForCareWithQAType request = new ListCertificatesForCareWithQAType();
        PersonId personId = new PersonId();
        personId.setExtension(personnummer.getPersonnummer());
        request.setPersonId(personId);
        HsaId hsaId = new HsaId();
        hsaId.setExtension(enhet);
        request.getEnhetsId().add(hsaId);

        ListCertificatesForCareWithQAResponseType response = responder.listCertificatesForCareWithQA("logicalAdress", request);

        Assert.assertNotNull(response);
    }
}
