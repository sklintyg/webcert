package se.inera.intyg.webcert.web.service.intyg.decorator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;

import java.util.List;

/**
 * This is a bit of a hack to mitigate states when an intyg is on a resend queue due to a 3rd party being unavailable
 * so IT cannot finish a send/revoke properly. Since the Intyg in IT won't be marked as sent/revoked until the operation
 * has successfully finished, the intyg state shown in Webcert GUI will be as NOT sent/revoked, even though we
 * have performed that operation and indicated in the GUI that we have sent or revoked it.
 *
 * Thus, we decorate the returned information with revoked/sent state from the Utkast if possible.
 */
@Component
public class UtkastIntygDecoratorImpl implements UtkastIntygDecorator {

    @Autowired
    private UtkastRepository utkastRepository;

    @Override
    public void decorateWithUtkastStatus(CertificateResponse certificate) {
        if (certificate.isRevoked()) {
            return;
        }

        boolean isSent = isSent(certificate.getMetaData().getStatus());
        boolean isRevoked = isRevoked(certificate.getMetaData().getStatus());
        if (isSent && isRevoked) {
            return;
        }
        Utkast utkast = utkastRepository.findOne(certificate.getMetaData().getCertificateId());

        // Don't try to decorate if utkast not found. May be a non-webcert intyg.
        if (utkast == null) {
            return;
        }

        if (utkast.getSkickadTillMottagareDatum() != null && !isSent) {
            // Add sent status flag
            Status sentStatus = new Status(CertificateState.SENT, utkast.getSkickadTillMottagare(), utkast.getSkickadTillMottagareDatum());
            certificate.getMetaData().getStatus().add(sentStatus);
        }

        if (utkast.getAterkalladDatum() != null && !isRevoked) {
            // Add revoked status flag
            Status revokedStatus = new Status(CertificateState.CANCELLED, "ANY", utkast.getAterkalladDatum());
            certificate.getMetaData().getStatus().add(revokedStatus);
        }
    }

    private boolean isRevoked(List<Status> statuses) {
        for (Status status : statuses) {
            if (status.getType() == CertificateState.CANCELLED) {
                return true;
            }
        }
        return false;
    }

    private boolean isSent(List<Status> statuses) {
        for (Status status : statuses) {
            if (status.getType() == CertificateState.SENT) {
                return true;
            }
        }
        return false;
    }
}
