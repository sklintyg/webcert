package se.inera.webcert.intygstjanststub;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;
import se.riv.clinicalprocess.healthcond.certificate.v1.Utlatande;
import se.riv.clinicalprocess.healthcond.certificate.v1.UtlatandeStatus;

/**
 * @author marced
 */
@Component
public class IntygStore {
    private static final Logger LOG = LoggerFactory.getLogger(IntygStore.class);

    private Map<String, GetCertificateForCareResponseType> intyg = new ConcurrentHashMap<>();

    public void addIntyg(GetCertificateForCareResponseType request) {
        LOG.debug("IntygStore: adding intyg " + request.getMeta().getCertificateId() + " to store.");
        if (intyg.containsKey(request.getMeta().getCertificateId() )) {
            LOG.debug("IntygStore: Not adding "  + request.getMeta().getCertificateId() + " to store. Is already present.");
            return;
        }
        intyg.put(request.getMeta().getCertificateId(), request);
    }

    public Map<String, GetCertificateForCareResponseType> getAllIntyg() {
        return intyg;
    }

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

    public GetCertificateForCareResponseType getIntygForCertificateId(String certificateId) {
        return intyg.get(certificateId);
    }

    public void updateUtlatande(Utlatande utlatande) {
        GetCertificateForCareResponseType getCertificateForCareResponseType = intyg.get(utlatande.getUtlatandeId().getExtension());
        if (getCertificateForCareResponseType != null) {
            getCertificateForCareResponseType.setCertificate(utlatande);
        }
    }

    public void addStatus(String extension, UtlatandeStatus status) {

        GetCertificateForCareResponseType getCertificateForCareResponseType = intyg.get(extension);
        if (getCertificateForCareResponseType != null) {
            getCertificateForCareResponseType.getMeta().getStatus().add(status);
        } else {

        }
    }

    public void clear() {
        intyg.clear();
    }
}
