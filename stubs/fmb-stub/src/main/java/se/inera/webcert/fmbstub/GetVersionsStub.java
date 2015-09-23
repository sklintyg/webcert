package se.inera.webcert.fmbstub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getversionsresponder.v1.GetVersionsResponderInterface;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getversionsresponder.v1.GetVersionsResponseType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getversionsresponder.v1.GetVersionsType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.VersionerType;

public class GetVersionsStub implements GetVersionsResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(GetVersionsStub.class);

    public GetVersionsStub() {
        LOG.info("Starting stub: FMB GetVersionsStub");
    }

    @Override
    public GetVersionsResponseType getVersions(String s, GetVersionsType getVersionsType) {
        final GetVersionsResponseType versionsResponse = new GetVersionsResponseType();
        final VersionerType versioner = new VersionerType();
        versioner.setFmbSenateAndring(String.valueOf(System.currentTimeMillis()));
        versioner.setDiagnosInformationSenateAndring(String.valueOf(System.currentTimeMillis()));
        versionsResponse.setVersioner(versioner);
        return versionsResponse;
    }

}
