package se.inera.intyg.webcert.web.service.intyg.decorator;

import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

import java.util.List;

/**
 * Created by eriklupander on 2017-05-18.
 */
public interface IntygRelationHelper {
    Relations getRelationsForIntyg(String intygId);

    void decorateIntygListWithRelations(List<ListIntygEntry> fullIntygItemList);
}
