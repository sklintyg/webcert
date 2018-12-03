package se.inera.intyg.webcert.web.service.fmb.sjukfall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.lang.invoke.MethodHandles;
import se.inera.intyg.schemas.contract.Personnummer;

@Service
public class FmbSjukfallServiceImpl implements FmbSjukfallService {

    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    @Override
    public void beraknaSjukfallForPatient(final Personnummer personnummer) {
        LOG.info("----------========={{{{{{ beraknaSjukfallForPatient }}}}}}}==========-----------");
    }
}
