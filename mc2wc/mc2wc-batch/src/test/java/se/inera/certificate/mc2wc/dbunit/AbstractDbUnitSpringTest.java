package se.inera.certificate.mc2wc.dbunit;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 * Abstract base class for data loading unit tests.
 *
 * @author nikpet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/application-context.xml", "classpath:/spring/rest-client-test-context.xml"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        DbUnitTestExecutionListener.class, TransactionalTestExecutionListener.class})
@DbUnitConfiguration(databaseConnection = "medcertDataSource", dataSetLoader = CustomFlatXmlDataSetLoader.class)
@ActiveProfiles("dev")
public abstract class AbstractDbUnitSpringTest {

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

}
