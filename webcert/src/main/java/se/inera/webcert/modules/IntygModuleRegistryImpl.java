package se.inera.webcert.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.ModuleApi;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static se.inera.certificate.modules.support.ApplicationOrigin.WEBCERT;

@Component
public class IntygModuleRegistryImpl implements IntygModuleRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(IntygModuleRegistryImpl.class);

    @Autowired
    private List<ModuleEntryPoint> moduleEntryPoints;

    private Map<String, ModuleApi> moduleApiMap = new HashMap<String, ModuleApi>();
    
    private List<IntygModule> moduleList = new ArrayList<IntygModule>();

    @PostConstruct
    private void initModulesList() {

        for (ModuleEntryPoint entryPoint : moduleEntryPoints) {
            moduleApiMap.put(entryPoint.getModuleId(), entryPoint.getModuleApi());
            IntygModule module = new IntygModule(entryPoint.getModuleId(), entryPoint.getModuleName(),
                    entryPoint.getModuleDescription(), entryPoint.isModuleFragaSvarAvailable(),
                    entryPoint.getModuleCssPath(WEBCERT), entryPoint.getModuleScriptPath(WEBCERT),
                    entryPoint.getModuleDependencyDefinitionPath(WEBCERT));

            moduleList.add(module);
        }

        Collections.sort(moduleList);

        LOG.info("Module registry loaded with {} modules", moduleApiMap.size());
    }

    @Override
    public List<IntygModule> listAllModules() {
        return moduleList;
    }

    @Override
    public ModuleApi getModuleApi(String id) {
        return moduleApiMap.get(id);
    }

    @Override
    public IntygModule getIntygModule(String id) {
        for (IntygModule m : moduleList) {
            if (m.getId().equals(id)) {
                return m;
            }
        }
        return null;
    }

    public List<ModuleEntryPoint> getModuleEntryPoints() {
        return moduleEntryPoints;
    }
    
}
