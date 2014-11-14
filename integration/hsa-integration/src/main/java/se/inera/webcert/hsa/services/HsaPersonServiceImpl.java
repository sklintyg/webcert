package se.inera.webcert.hsa.services;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonResponseType;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonType;
import se.inera.ifv.hsawsresponder.v3.GetMiuForPersonResponseType;
import se.inera.ifv.hsawsresponder.v3.GetMiuForPersonType;
import se.inera.ifv.hsawsresponder.v3.MiuInformationType;
import se.inera.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;
import se.inera.webcert.hsa.stub.Medarbetaruppdrag;

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
    
    
    public List<MiuInformationType> checkIfPersonHasMIUsOnUnit(String hosPersonHsaId, final String unitHsaId) {
    	
    	LOG.debug("Checking if person with HSA id '{}' has MIUs on unit '{}'", hosPersonHsaId, unitHsaId);
    	
    	GetMiuForPersonType parameters = new GetMiuForPersonType();
    	parameters.setHsaIdentity(hosPersonHsaId);
    	
		GetMiuForPersonResponseType response = client.callMiuRights(parameters);
		
		if (response == null) {
			LOG.debug("Response from HSA was null, no valid MIUs was found for person '{}'", hosPersonHsaId);
			return Lists.newArrayList();
		}
		
		List<MiuInformationType> miusForPerson = response.getMiuInformation();
		
		if (miusForPerson.isEmpty()) {
			LOG.debug("Response from HSA was empty, no MIUs was found for person '{}'", hosPersonHsaId);
			return Lists.newArrayList();
		}
		
		LOG.debug("Person has a total of {} MIUs", miusForPerson.size());
		
		Predicate<MiuInformationType> predicate = new Predicate<MiuInformationType>() {
			@Override
			public boolean apply(MiuInformationType miu) {
				
				String miuHsaId = miu.getHsaIdentity();
				
				LOG.debug("Checking MIU '{}' for unit '{}'", miuHsaId, miu.getCareUnitHsaIdentity());
				
				if (checkMiuMatch(miu.getCareUnitHsaIdentity())) {
					
					if (!checkMiuPurpose(miu.getMiuPurpose())) {
						LOG.debug("- MIU '{}' is not '{}'", miuHsaId, Medarbetaruppdrag.VARD_OCH_BEHANDLING);
						return false;
					}
					
					if (!checkMiuNotExpired(miu.getCareUnitEndDate())) {
						LOG.debug("- MIU '{}' is expired", miuHsaId);
						return false;
					}
					
					LOG.debug("+ MIU '{}' is for the right unit and valid", miuHsaId);
					return true;	
				}
				LOG.debug("- MIU '{}' is for another unit", miuHsaId);
				return false;
			}
			
			// careUnitEndDate can be null, which does not mean that the thing expired
			private boolean checkMiuNotExpired(LocalDateTime careUnitEndDate) {
				return (careUnitEndDate != null) ? careUnitEndDate.isAfter(LocalDateTime.now()) : true;
			}

			private boolean checkMiuPurpose(String miuPurpose) {
				return (miuPurpose != null) ? Medarbetaruppdrag.VARD_OCH_BEHANDLING.equalsIgnoreCase(miuPurpose) : false;
			}

			private boolean checkMiuMatch(String miuUnitHsaId) {
				return (unitHsaId.equals(miuUnitHsaId));
			}
		};
		
		List<MiuInformationType> miusOnUnit = Lists.newArrayList(Collections2.filter(miusForPerson, predicate));
		
		LOG.debug("Person has {} MIUs on unit '{}'", miusOnUnit.size(), hosPersonHsaId);
		
		return miusOnUnit;
    }

}
