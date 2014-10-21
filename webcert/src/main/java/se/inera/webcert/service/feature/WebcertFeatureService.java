package se.inera.webcert.service.feature;

import java.util.Set;

import se.inera.certificate.modules.support.feature.ModuleFeature;

/**
 * Service which keeps track of what features are active in Webcert and installed modules.
 * 
 * @author npet
 *
 */
public interface WebcertFeatureService {

    /**
     * Checks if a Webcert or module feature is active. The name of a module feature
     * needs to be fully qualified with module name.
     * 
     * @param featureName
     * @return
     */
    public abstract boolean isFeatureActive(String featureName);

    /**
     * Checks if a Webcert feature is active.
     * 
     * @param feature The Webcert feature enum
     * @return
     */
    public abstract boolean isFeatureActive(Features feature);
    
    /**
     * Check if a module feature is active.
     * 
     * @param moduleFeature The module feature name
     * @param moduleName The name of the module
     * @return
     */
    public boolean isModuleFeatureActive(String modelFeatureName, String moduleName);
    
    /**
     * Check if a module feature is active
     * 
     * @param moduleFeature The module feature as Enum
     * @param moduleName The name of the module
     * @return
     */
    public boolean isModuleFeatureActive(ModuleFeature moduleFeature, String moduleName);
    
    /**
     * Returns a Set containing the names of all features, Webcert and module, that are active.
     * 
     * @return
     */
    public abstract Set<String> getActiveFeatures();

}
