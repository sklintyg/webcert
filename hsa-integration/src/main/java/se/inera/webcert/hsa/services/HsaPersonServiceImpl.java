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
import se.inera.webcert.hsa.model.Specialisering;

/**
 * @author nikpet
 * 
 */
@Service
public class HsaPersonServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(HsaPersonServiceImpl.class);

    @Autowired
    private HSAWebServiceCalls client;

    public List<Specialisering> getSpecialitiesForHsaPerson(String personHsaId) {
        
        LOG.debug("Getting specialities for person '{}'", personHsaId);
        
        List<GetHsaPersonHsaUserType> personHsaInfos = getHsaPersonInfo(personHsaId);

        if (personHsaInfos.isEmpty()) {
            return new ArrayList<>();
        }

        List<Specialisering> userSpecialities = extractSpecialitiesFromUsers(personHsaInfos);
        
        LOG.debug("Person '{}' has {} specialities", personHsaId, userSpecialities.size());
        
        return userSpecialities;
    }

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

    private List<Specialisering> extractSpecialitiesFromUsers(List<GetHsaPersonHsaUserType> userTypeList) {

        List<Specialisering> specialities = new ArrayList<Specialisering>();

        for (GetHsaPersonHsaUserType userType : userTypeList) {
            collectSpecialities(userType, specialities);
        }        
                
        return specialities;
    }

    private void collectSpecialities(GetHsaPersonHsaUserType userType, List<Specialisering> specialities) {

        List<String> userSpecCodes = userType.getSpecialityCodes().getSpecialityCode();
        List<String> userSpecNames = userType.getSpecialityNames().getSpecialityName();

        int maxLength = userSpecCodes.size();

        // TODO: This assumes that both lists are equally long. What if not?

        Specialisering spec;

        for (int i = 0; i < maxLength; i++) {
            spec = new Specialisering(userSpecCodes.get(i), userSpecNames.get(i));
            specialities.add(spec);
        }
    }
}
