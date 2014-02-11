package se.inera.webcert.service.draft.util;

/**
 * Strategy for generating an id for a new Intyg.
 * 
 * @author nikpet
 *
 */
public interface CreateIntygsIdStrategy {
    
    public String createId();
    
}
