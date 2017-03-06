package se.inera.intyg.webcert.web.integration;

import static se.inera.intyg.common.support.Constants.KV_HANDELSE_CODE_SYSTEM;
import static se.inera.intyg.webcert.notification_sender.notifications.services.NoricationTypeConverter.toArenden;

import java.util.stream.Collectors;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotifications;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v2.HandelseList;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v2.List;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v2.ListCertificatesForCareWithQAResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v2.ListCertificatesForCareWithQAResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v2.ListCertificatesForCareWithQAType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v2.ListItem;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Handelsekod;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v2.Handelse;

@SchemaValidation
public class ListCertificatesForCareWithQAResponderImpl implements ListCertificatesForCareWithQAResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(ListCertificatesForCareWithQAResponderImpl.class);

    @Autowired
    private IntygService intygService;

    private static Handelse toHandelse(se.inera.intyg.webcert.persistence.handelse.model.Handelse e) {
        Handelse res = new Handelse();

        Handelsekod code = new Handelsekod();
        code.setCodeSystem(KV_HANDELSE_CODE_SYSTEM);
        code.setCode(e.getCode().value());
        res.setHandelsekod(code);
        res.setTidpunkt(e.getTimestamp());

        return res;
    }

    @Override
    public ListCertificatesForCareWithQAResponseType listCertificatesForCareWithQA(String s, ListCertificatesForCareWithQAType request) {
        ListCertificatesForCareWithQAResponseType response = new ListCertificatesForCareWithQAResponseType();
        List list = new List();

        java.util.List<IntygWithNotifications> intygWithNotifications = intygService.listCertificatesForCareWithQA(
                new Personnummer(request.getPersonId().getExtension()),
                request.getEnhetsId().stream().map(HsaId::getExtension).collect(Collectors.toList()));

        for (IntygWithNotifications intygHolder : intygWithNotifications) {
            ListItem item = new ListItem();
            item.setIntyg(intygHolder.getIntyg());
            HandelseList handelseList = new HandelseList();
            handelseList.getHandelse().addAll(intygHolder.getNotifications().stream()
                    .map(ListCertificatesForCareWithQAResponderImpl::toHandelse)
                    .collect(Collectors.toList()));
            item.setHandelser(handelseList);
            item.setSkickadeFragor(toArenden(intygHolder.getSentQuestions()));
            item.setMottagnaFragor(toArenden(intygHolder.getReceivedQuestions()));
            list.getItem().add(item);
        }
        response.setList(list);
        response.setResult(ResultTypeUtil.okResult());
        return response;
    }
}
