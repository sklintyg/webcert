package se.inera.webcert.hsa.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonResponseType;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonType;
import se.inera.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;

/**
 * @author nikpet
 */
@Service
public class HsaPersonServiceImpl implements HsaPersonService {

    private static final Logger LOG = LoggerFactory.getLogger(HsaPersonServiceImpl.class);

    @Autowired
    private HSAWebServiceCalls client;

    /* (non-Javadoc)
     * @see se.inera.webcert.hsa.services.HsaPersonService#getHsaPersonInfo(java.lang.String)
     */
    @Override
    public List<GetHsaPersonHsaUserType> getHsaPersonInfo(String personHsaId) {

        LOG.debug("Getting info from HSA for person '{}'", personHsaId);

        GetHsaPersonType parameters = new GetHsaPersonType();
        parameters.setHsaIdentity(personHsaId);

        GetHsaPersonResponseType response = client.callGetHsaPerson(parameters);

        if (response == null || response.getUserInformations() == null) {
            LOG.debug("Response did not contain any person info for HSA id '{}'", personHsaId);
            return new ArrayList<>();
        }

        List<GetHsaPersonHsaUserType> hsaUserTypeList = response.getUserInformations().getUserInformation();

        LOG.debug("Returning {} information objects for HSA id '{}'", hsaUserTypeList.size(), personHsaId);

        return hsaUserTypeList;
    }

}
