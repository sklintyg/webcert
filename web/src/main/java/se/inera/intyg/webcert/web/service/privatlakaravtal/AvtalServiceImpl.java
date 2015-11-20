package se.inera.intyg.webcert.web.service.privatlakaravtal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.webcert.persistence.privatlakaravtal.repository.AvtalRepository;
import se.inera.webcert.persistence.privatlakaravtal.repository.GodkantAvtalRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

/**
 * Created by eriklupander on 2015-08-05.
 */
@Service("AvtalService")
public class AvtalServiceImpl implements AvtalService {

    @Autowired
    private AvtalRepository avtalRepository;

    @Autowired
    private GodkantAvtalRepository godkantAvtalRepository;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
    public boolean userHasApprovedLatestAvtal(String userId) {
        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();
        return godkantAvtalRepository.userHasApprovedAvtal(userId, latestAvtalVersion);
    }

    @Override
    public Avtal getLatestAvtal() {
        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();
        return avtalRepository.findOne(latestAvtalVersion);
    }

    @Override
    public void approveLatestAvtal(String userId, String personId) {
        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();
        if ((latestAvtalVersion == null) || (latestAvtalVersion == -1)) {
            throw new IllegalStateException("Cannot approve private practitioner avtal, no avtal exists in the database.");
        }
        godkantAvtalRepository.approveAvtal(userId, latestAvtalVersion);
        monitoringLogService.logPrivatePractitionerTermsApproved(userId, personId, latestAvtalVersion);
    }

    @Override
    public void removeApproval(String userId) {
        godkantAvtalRepository.removeAllUserApprovments(userId);
    }
}
