package se.inera.webcert.modules;

import se.inera.webcert.modules.api.ModuleRestApi;

public interface ModuleRestApiFactory {

    /**
     * Creates a {@link ModuleRestApi} for the given module name.
     */
    public abstract ModuleRestApi getModuleRestService(String moduleName);

}
