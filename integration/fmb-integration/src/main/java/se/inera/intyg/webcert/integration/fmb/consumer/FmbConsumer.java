package se.inera.intyg.webcert.integration.fmb.consumer;

import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmdxInformation;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Typfall;

public interface FmbConsumer {

    Typfall getTypfall() throws FailedToFetchFmbData;

    FmdxInformation getForsakringsmedicinskDiagnosinformation() throws FailedToFetchFmbData;

}
