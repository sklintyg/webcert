package se.inera.webcert.intygstjanststub;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.ifv.insuranceprocess.certificate.v1.CertificateMetaType;

import com.google.common.collect.Iterables;

/**
 * @author marced
 */
@Component
public class IntygStore {
    private static final Logger LOG = LoggerFactory.getLogger(IntygStore.class);

    private ConcurrentHashMap<String, GetCertificateForCareResponseType> intyg = new ConcurrentHashMap<String, GetCertificateForCareResponseType>();

    public void addIntyg(GetCertificateForCareResponseType request) {
        LOG.debug("IntygStore: added intyg " + request.getMeta().getCertificateId() + " to store.");
        intyg.put(request.getMeta().getCertificateId(), request);
    }

    public ConcurrentHashMap<String, GetCertificateForCareResponseType> getAllIntyg() {
        return intyg;
    }

    public Iterable<CertificateMetaType> getIntygForEnhetAndPersonnummer(final List<String> enhetsIds,
            final String personnummer) {
        Iterable<GetCertificateForCareResponseType> filtered = Iterables.filter(intyg.values(),
                new Predicate<GetCertificateForCareResponseType>() {
                    @Override
                    public boolean apply(GetCertificateForCareResponseType i) {
                        return enhetsIds.contains(i.getCertificate().getSkapadAv().getEnhet().getEnhetsId()
                                .getExtension())
                                && personnummer.equals(i.getCertificate().getPatient().getPersonId().getExtension());

                    }
                });

        return Iterables.transform(filtered, new Function<GetCertificateForCareResponseType, CertificateMetaType>() {
            @Override
            public CertificateMetaType apply(GetCertificateForCareResponseType input) {
                return input.getMeta();
            }
        });
    }
}
