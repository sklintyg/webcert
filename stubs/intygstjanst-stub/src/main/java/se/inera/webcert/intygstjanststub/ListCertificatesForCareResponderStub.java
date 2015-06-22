package se.inera.webcert.intygstjanststub;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;
import se.riv.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;

import com.google.common.collect.Lists;

/**
 * @author andreaskaltenbach
 */
public class ListCertificatesForCareResponderStub implements ListCertificatesForCareResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    public ListCertificatesForCareResponseType listCertificatesForCare(String s, ListCertificatesForCareType request) {

        ListCertificatesForCareResponseType response = new ListCertificatesForCareResponseType();

        Iterable<CertificateMetaType> certsIterable = intygStore.getIntygForEnhetAndPersonnummer(request.getEnhet(), request.getPersonId());

        List<CertificateMetaType> certs = Lists.newArrayList(certsIterable);
        response.getMeta().addAll(certs);

        ResultType result = new ResultType();
        result.setResultCode(ResultCodeType.OK);
        response.setResult(result);

        return response;
    }
}
