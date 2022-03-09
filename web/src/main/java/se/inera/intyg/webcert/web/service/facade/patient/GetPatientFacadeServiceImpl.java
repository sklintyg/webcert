package se.inera.intyg.webcert.web.service.facade.patient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.facade.PatientController;
import se.inera.intyg.webcert.web.web.controller.facade.dto.PatientResponseDTO;

@Service
public class GetPatientFacadeServiceImpl {
    private static final Logger LOG = LoggerFactory.getLogger(PatientController.class);

    final private PUService puService;

    final private MonitoringLogService monitoringService;

    @Autowired
    public GetPatientFacadeServiceImpl(PUService puService, MonitoringLogService monitoringService) {
        this.puService = puService;
        this.monitoringService = monitoringService;
    }

    public PatientResponseDTO getPatient(String patientId) {
        try {
            Personnummer formattedPatientId = formatPatientId(patientId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting patient info for: {}", formattedPatientId.getPersonnummerHash());
            }

            PersonSvar personSvar = puService.getPerson(formattedPatientId);

            monitoringService.logPULookup(formattedPatientId, personSvar.getStatus().name());

            return convertPatientResponse(personSvar, patientId);

        } catch (InvalidPersonNummerException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    private PatientResponseDTO convertPatientResponse(PersonSvar personSvar, String patientId) {
        final var patient = Patient.builder()
                .personId(
                        PersonId.builder()
                                .id(patientId)
                                .type("")
                                .build()
                )
                .firstName(personSvar.getPerson().getFornamn())
                .lastName(personSvar.getPerson().getEfternamn())
                .middleName(personSvar.getPerson().getMellannamn())
                .deceased(personSvar.getPerson().isAvliden())
                .protectedPerson(personSvar.getPerson().isSekretessmarkering())
                .testIndicated(personSvar.getPerson().isTestIndicator())
                .build();

        return PatientResponseDTO.create(patient, personSvar.getStatus());
    }

    private Personnummer formatPatientId(String personId) throws InvalidPersonNummerException {
        return Personnummer.createPersonnummer(personId)
                .orElseThrow(() -> new InvalidPersonNummerException("Could not create Personnummer object from personId: " + personId));
    }
}
