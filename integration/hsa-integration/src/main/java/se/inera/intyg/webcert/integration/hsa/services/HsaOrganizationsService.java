package se.inera.webcert.hsa.services;

import java.util.List;

import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;

/**
 * @author andreaskaltenbach
 */
public interface HsaOrganizationsService {

    /**
     * Returns a list of Vardgivare and authorized enheter where the HoS person is authorized to work at.
     *
     * @return list of vårdgivare containing authorized enheter and mottagningar. If user is not authorized at all,
     *         an empty list will be returned
     */
    List<Vardgivare> getAuthorizedEnheterForHosPerson(String hosPersonHsaId);

    Vardenhet getVardenhet(String vardenhetHsaId);
}
