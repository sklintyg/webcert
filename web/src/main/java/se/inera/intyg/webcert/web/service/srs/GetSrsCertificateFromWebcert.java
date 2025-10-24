package se.inera.intyg.webcert.web.service.srs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.lisjp.v1.model.internal.LisjpUtlatandeV1;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.converter.util.ConverterException;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;

@Slf4j
@RequiredArgsConstructor
@Service("GetSrsCertificateFromWC")
public class GetSrsCertificateFromWebcert implements GetSrsCertificate {

  private final IntygModuleFacade intygModuleFacade;
  private final IntygService intygService;

  @Override
  public SrsCertificate getSrsCertificate(String certificateId) {
    log.debug("getExtensionChain(certificateId:{})", certificateId);
    try {
      final var currentModel = getModelForCertificateId(certificateId);
      if (currentModel == null) {
        log.debug("No model found for certificate id {}", certificateId);
        return null;
      }
      final var lisjpUtlatandeV1 = getLispjV1UtlatandeFromModel(currentModel);
      return buildSrsCertFromUtlatande(lisjpUtlatandeV1);
    } catch (ConverterException e) {
      log.error("Couldn't convert certificate to correct type while decorating extension chain for SRS", e);
      return null;
    }
  }

  private IntygContentHolder getCertificate(String certificateId) {
    return intygService.fetchIntygDataWithRelations(certificateId, LisjpEntryPoint.MODULE_ID);
  }

  private String getModelForCertificateId(String certificateId) {
    if (StringUtils.isBlank(certificateId)) {
      return null;
    }
    IntygContentHolder currentCert = getCertificate(certificateId); // will fallback to check in Webcert if no hit in cert. service
    return currentCert != null ? currentCert.getContents() : null;
  }

  private LisjpUtlatandeV1 getLispjV1UtlatandeFromModel(String model) throws ConverterException {
    Utlatande utlatande = intygModuleFacade.getUtlatandeFromInternalModel(LisjpEntryPoint.MODULE_ID, model);
    if (!(utlatande instanceof LisjpUtlatandeV1)) {
      throw new ConverterException("Utlatande is not of type LisjpUtlatandeV1, utlatande: " + utlatande);
    }
    return (LisjpUtlatandeV1) utlatande;
  }

  private SrsCertificate buildSrsCertFromUtlatande(
      LisjpUtlatandeV1 lisjpUtlatandeV1) throws ConverterException {
    if (lisjpUtlatandeV1 == null) {
      throw new ConverterException("Utlatande to convert to SrsCert was null");
    }
    SrsCertificate srsCert = new SrsCertificate(lisjpUtlatandeV1.getId());
    if (lisjpUtlatandeV1.getGrundData() != null && lisjpUtlatandeV1.getGrundData().getSigneringsdatum() != null) {
      srsCert.setSignedDate(lisjpUtlatandeV1.getGrundData().getSigneringsdatum().toLocalDate());
    }
    if (lisjpUtlatandeV1.getDiagnoser() != null && !lisjpUtlatandeV1.getDiagnoser().isEmpty()) {
      srsCert.setMainDiagnosisCode(lisjpUtlatandeV1.getDiagnoser().getFirst().getDiagnosKod());
    }
    if (lisjpUtlatandeV1.getGrundData().getRelation() != null
        && !lisjpUtlatandeV1.getGrundData().getRelation().getRelationIntygsId().isEmpty()
        && lisjpUtlatandeV1.getGrundData().getRelation().getRelationKod() == RelationKod.FRLANG) {
      srsCert.setExtendsCertificateId(lisjpUtlatandeV1.getGrundData().getRelation().getRelationIntygsId());
    }
    log.debug("SrsCertificate(id:{}, mainDiagCode:{}, signedDate:{})",
        srsCert.getCertificateId(), srsCert.getMainDiagnosisCode(), srsCert.getSignedDate());
    return srsCert;
  }
}
