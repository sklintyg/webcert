package se.inera.webcert.integration.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.HsaId;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.PersonId;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.UtlatandeTyp;
import se.inera.certificate.modules.registry.IntygModuleRegistry;

@RunWith(MockitoJUnitRunner.class)
public class CreateDraftCertificateValidatorTest {

    private static final String HSAID_ROOT = "1.2.752.129.2.1.4.1";

    private static final String FK7263 = "fk7263";
    
    private static final String VALID_HSAID = "SE2321000016-A1PB";
    
    private static final String INVALID_HSAID = "SE12345678-123A";

    private static final String PERSONNR_ROOT = "1.2.752.129.2.1.3.1";
    
    private static final String SAMMORDNNR_ROOT = "1.2.752.129.2.1.3.3";
    
    @Mock
    private IntygModuleRegistry moduleRegistry;

    @InjectMocks
    private CreateDraftCertificateValidatorImpl validator;

    @Before
    public void setup() {
        validator.init();
    }

    @Before
    public void setupExpectations() {
        Answer<?> moduleRegistryAnswer = new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String moduleId = (String) args[0];
                return (FK7263.equals(moduleId));
            }
        };
        when(moduleRegistry.moduleExists(FK7263)).then(moduleRegistryAnswer);
    }

    @Test
    public void testValidateTypAvUtlatandeValid() {
        
        UtlatandeTyp typAvUtlatande = new UtlatandeTyp();
        typAvUtlatande.setCode(FK7263);
        
        ValidationResult res = new ValidationResult();
                
        validator.validateTypAvUtlatande(typAvUtlatande , res);
        assertFalse(res.hasErrors());
    }
    
    @Test
    public void testValidateTypAvUtlatandeInvalid() {
        
        UtlatandeTyp typAvUtlatande = new UtlatandeTyp();
        typAvUtlatande.setCode("SomethingElse");
        
        ValidationResult res = new ValidationResult();
                
        validator.validateTypAvUtlatande(typAvUtlatande , res);
        assertTrue(res.hasErrors());
    }
    
    @Test
    public void testEmptyId() {

        HsaId hsaId = new HsaId();
        ValidationResult res = new ValidationResult();

        validator.validateId(hsaId, "empty-hsa-id", res);
        assertTrue(res.hasErrors());
    }

    @Test
    public void testHsaIdUnsupportedRootUid() {
        
        HsaId hsaId = new HsaId();
        hsaId.setRoot("0.1.123.123.0.0.0.0");
        hsaId.setExtension("SE234234");
        
        ValidationResult res = new ValidationResult();

        validator.validateId(hsaId, "wrong-uid-hsa-id", res);
        assertTrue(res.hasErrors());
        
    }
    
    @Test
    public void testHsaIdMalformed() {

        HsaId hsaId = new HsaId();
        hsaId.setRoot(HSAID_ROOT);
        hsaId.setExtension(INVALID_HSAID);

        ValidationResult res = new ValidationResult();

        validator.validateId(hsaId, "malformed-hsa-id", res);
        assertTrue(res.hasErrors());
    }
    
    @Test
    public void testHsaIdCorrect() {

        HsaId hsaId = new HsaId();
        hsaId.setRoot(HSAID_ROOT);
        hsaId.setExtension(VALID_HSAID);

        ValidationResult res = new ValidationResult();

        validator.validateId(hsaId, "valid-hsa-id", res);
        assertFalse(res.hasErrors());
    }
    
    @Test
    public void testPersonIdMalformed() {
     
        PersonId pid = new PersonId();
        pid.setRoot(PERSONNR_ROOT);
        pid.setExtension("198209-9290");
        
        ValidationResult res = new ValidationResult();

        validator.validateId(pid, "malformed-person-id", res);
        assertTrue(res.hasErrors());
    }
    
    @Test
    public void testPersonIdNoDash() {
    
        PersonId pid = new PersonId();
        pid.setRoot(PERSONNR_ROOT);
        pid.setExtension("198210099290");
        
        ValidationResult res = new ValidationResult();
    
        validator.validateId(pid, "nodash-person-id", res);
        assertTrue(res.hasErrors());
    }

    @Test
    public void testPersonIdInvalidChecksum() {
    
        PersonId pid = new PersonId();
        pid.setRoot(PERSONNR_ROOT);
        pid.setExtension("19821009-9390");
        
        ValidationResult res = new ValidationResult();
    
        validator.validateId(pid, "invalidchecksum-person-id", res);
        assertTrue(res.hasErrors());
    }

    @Test
    public void testPersonIdCorrect() {
     
        PersonId pid = new PersonId();
        pid.setRoot(PERSONNR_ROOT);
        pid.setExtension("19821009-9290");
        
        ValidationResult res = new ValidationResult();

        validator.validateId(pid, "valid-person-id", res);
        assertFalse(res.hasErrors());
    }
    
    @Test
    public void testPersonIdSammordningsNrCorrect() {
     
        PersonId pid = new PersonId();
        pid.setRoot(SAMMORDNNR_ROOT);
        pid.setExtension("19801066-9235");
        
        ValidationResult res = new ValidationResult();

        validator.validateId(pid, "valid-person-id", res);
        assertFalse(res.hasErrors());
    }
}
