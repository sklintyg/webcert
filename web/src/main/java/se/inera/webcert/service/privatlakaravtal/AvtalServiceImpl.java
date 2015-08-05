package se.inera.webcert.service.privatlakaravtal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.webcert.persistence.privatlakaravtal.repository.AvtalRepository;
import se.inera.webcert.persistence.privatlakaravtal.repository.GodkantAvtalRepository;

/**
 * Created by eriklupander on 2015-08-05.
 */
@Service
public class AvtalServiceImpl implements AvtalService {

    @Autowired
    AvtalRepository avtalRepository;

    @Autowired
    GodkantAvtalRepository godkantAvtalRepository;

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
    public void approveLatestAvtal(String userId) {
        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();
        godkantAvtalRepository.approveAvtal(userId, latestAvtalVersion);
    }
}
