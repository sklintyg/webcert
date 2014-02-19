package se.inera.webcert.modules.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

/**
 * Mock implementation of {@link IntygModuleRegistry} using a map that is initialized
 * at bean creation.
 * 
 * @author nikpet
 *
 */
@Component
public class MockIntygModulesRegistryImpl implements IntygModuleRegistry {

    private Map<String, IntygModule> modulesMap = new HashMap<String, IntygModule>();
    
    public MockIntygModulesRegistryImpl() {
        
    }

    @PostConstruct
    public void initMockRegistry() {
        
        int sortCount = 1;
        
        IntygModule im = new IntygModule("fk7263", "Läkarintyg FK 7263", "fk7263", sortCount++);
        modulesMap.put(im.getId(), im);
        
        im = new IntygModule("ts-bas", "Transportstyrelsens läkarintyg, bas", "ts-bas", sortCount++);
        modulesMap.put(im.getId(), im);
        
        im = new IntygModule("ts-diabetes", "Transportstyrelsens läkarintyg, diabetes", "ts-diabetes", sortCount++);
        modulesMap.put(im.getId(), im);
        
        im = new IntygModule("ivar", "Intyg vid avbeställd resa", "rli", sortCount++);
        modulesMap.put(im.getId(), im);
        
    }
    
    /* (non-Javadoc)
     * @see se.inera.webcert.module.IntygModuleRegistry#getModule(java.lang.String)
     */
    @Override
    public IntygModule getModule(String name) {
        return modulesMap.get(name);
    }
    
    /* (non-Javadoc)
     * @see se.inera.webcert.module.IntygModuleRegistry#listAllModules()
     */
    @Override
    public List<IntygModule> listAllModules() {
        List<IntygModule> moduleList = new ArrayList<IntygModule>();
        moduleList.addAll(modulesMap.values());
        Collections.sort(moduleList);
        return moduleList;
    }
    
}
