package se.inera.webcert.web.service;

/**
 * @author johannesc
 */
public interface HsaService {
    /**
     * Returns all vardenheter and their mottagningar where the user has medarbetaruppdrag.
     * @param userHsaId
     * @return
     */
    Vardenheter getVardenheterMedMedarbetaruppdrag(String userHsaId);
}
