package se.inera.webcert.service;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.riv.ehr.log.store.storelog.v1.StoreLogRequestType;
import se.riv.ehr.log.store.storelog.v1.StoreLogResponderInterface;
import se.riv.ehr.log.v1.ActivityType;
import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.SystemType;
import se.riv.ehr.log.v1.UserType;

/**
 * @author andreaskaltenbach
 */
@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private StoreLogResponderInterface storeLogInterface;

    @Override
    public void logReadOfIntyg(String utlatandeId) {


        StoreLogRequestType request = new StoreLogRequestType();
        LogType log = new LogType();

        SystemType system = new SystemType();
        //system.setSystemId();
        //...
        log.setSystem(system);

        ActivityType activity = new ActivityType();
        activity.setStartDate(new LocalDateTime());
        activity.setActivityType("Läsa");
        //...
        log.setActivity(activity);

        UserType user = new UserType();
        //user.setName();
        //...
        log.setUser(user);

        request.getLog().add(log);

        storeLogInterface.storeLog("SE165565594230-1000", request);
    }
}
