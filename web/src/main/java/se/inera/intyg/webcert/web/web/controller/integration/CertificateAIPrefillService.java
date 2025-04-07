package se.inera.intyg.webcert.web.web.controller.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateAIPrefillService {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final Cache redisCacheLaunchId;

    @Async("threadPoolTaskExecutor")
    public void prefill(String certificateId, IntegrationParameters parameters) {

        if (parameters.getPrefillData() == null || parameters.getPrefillData().isBlank()) {
            return;
        }

        final var launchId = parameters.getLaunchId();
        final var redisCacheKey = "prefillInProgress:" + launchId;

        // Cache i redis med launch id att prefill pågår.
        redisCacheLaunchId.put(redisCacheKey, "prefillInProgress");
        log.info("Prefill in progress for launchId: {}", launchId);

        csIntegrationService.certificateAIPrefill(
            certificateId,
            csIntegrationRequestFactory.certificateAIPrefillRequest(parameters.getPrefillData())
        );

        redisCacheLaunchId.put(redisCacheKey, null);
        log.info("Prefill completed for launchId: {}", launchId);
    }
}
