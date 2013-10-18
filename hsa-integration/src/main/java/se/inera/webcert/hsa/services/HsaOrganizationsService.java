package se.inera.webcert.hsa.services;

import java.util.List;

import se.inera.webcert.hsa.model.Vardgivare;

/**
 * @author andreaskaltenbach
 */
public interface HsaOrganizationsService {

    /**
     * Returns a list of Vardgivare and authorized enheter where the HoS person is authorized to work at.
     */
    List<Vardgivare> getAuthorizedEnheterForHosPerson(String hosPersonHsaId);
}
