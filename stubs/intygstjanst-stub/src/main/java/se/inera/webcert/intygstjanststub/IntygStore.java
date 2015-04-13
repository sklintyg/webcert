package se.inera.webcert.intygstjanststub;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import se.inera.webcert.intygstjanststub.mode.StubModeAware;

/**
 * @author marced
 */
@Component
public class IntygStore {
    private static final Logger LOG = LoggerFactory.getLogger(IntygStore.class);

    private Map<String, GetCertificateForCareResponseType> intyg = new ConcurrentHashMap<>();

    @StubModeAware
    public void addIntyg(GetCertificateForCareResponseType request) {
        LOG.debug("IntygStore: added intyg " + request.getMeta().getCertificateId() + " to store.");
        intyg.put(request.getMeta().getCertificateId(), request);
    }

    @StubModeAware
    public Map<String, GetCertificateForCareResponseType> getAllIntyg() {
        return intyg;
    }

    @StubModeAware
    public Iterable<CertificateMetaType> getIntygForEnhetAndPersonnummer(final List<String> enhetsIds,
            final String personnummer) {
        Iterable<GetCertificateForCareResponseType> filtered = Iterables.filter(intyg.values(),
                new Predicate<GetCertificateForCareResponseType>() {
                    @Override
                    public boolean apply(GetCertificateForCareResponseType i) {
                        return enhetsIds.contains(i.getCertificate().getSkapadAv().getEnhet().getEnhetsId().getExtension())
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
