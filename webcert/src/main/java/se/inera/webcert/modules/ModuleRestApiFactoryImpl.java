package se.inera.webcert.modules;

import java.util.Collections;
import java.util.List;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import se.inera.webcert.modules.api.ModuleRestApi;
import se.inera.webcert.modules.registry.IntygModule;
import se.inera.webcert.modules.registry.IntygModuleRegistry;

/**
 * Factory for creating a REST client for communicating with a specific module.
 * 
 * @author nikpet
 *
 */
@Component
public class ModuleRestApiFactoryImpl implements ModuleRestApiFactory {

    private static Logger LOG = LoggerFactory.getLogger(ModuleRestApiFactoryImpl.class);
    
    @Autowired
    private IntygModuleRegistry moduleRegistry;
    
    @Autowired
    private JacksonJaxbJsonProvider jacksonJsonProvider;

    private String host;

    public ModuleRestApiFactoryImpl() {
        
    }

    @Autowired
    @Value("${modules.port}")
    public void setPort(String port) {
        host = "http://localhost:" + port;
    }

    /* (non-Javadoc)
     * @see se.inera.webcert.modules.ModuleRestApiFactory#getModuleRestService(java.lang.String)
     */
    @Override
    public ModuleRestApi getModuleRestService(String moduleName) {
        
        LOG.debug("Getting module REST service for module {}", moduleName);
        
        IntygModule intygModule = moduleRegistry.getModule(moduleName);
        
        if (intygModule == null) {
            LOG.error("Module entry for moduleName '{}' was not found in the module registry!", moduleName);
            return null;
        }
        
        String moduleApiUri = composeApiURI(intygModule);
        
        LOG.debug("URI is {}", moduleApiUri);
        
        ModuleRestApi client = JAXRSClientFactory.create(moduleApiUri, ModuleRestApi.class, Collections.singletonList(jacksonJsonProvider));
        
        return client;
    }
    
    private String composeApiURI(IntygModule intygModule) {
        
        StringBuilder sb = new StringBuilder();
        sb.append(host).append("/");
        sb.append(intygModule.getUrl());
        sb.append("/api");
        
        return sb.toString();
    }
    
}
