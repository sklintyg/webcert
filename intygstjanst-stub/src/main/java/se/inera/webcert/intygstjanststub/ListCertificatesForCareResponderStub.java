package se.inera.webcert.intygstjanststub;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;
import se.inera.ifv.insuranceprocess.certificate.v1.CertificateMetaType;

/**
 * @author andreaskaltenbach
 */
public class ListCertificatesForCareResponderStub implements ListCertificatesForCareResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    public ListCertificatesForCareResponseType listCertificatesForCare(String s, ListCertificatesForCareType request) {

        ListCertificatesForCareResponseType response = new ListCertificatesForCareResponseType();
        
        Iterable<CertificateMetaType> meta = intygStore.getIntygForEnhetAndPersonnummer(request.getCareUnit(), request.getNationalIdentityNumber());

        response.getMeta().addAll(Lists.newArrayList(meta));

        return response;
    }
}
