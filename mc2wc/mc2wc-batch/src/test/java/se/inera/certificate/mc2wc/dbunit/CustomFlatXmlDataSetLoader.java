package se.inera.certificate.mc2wc.dbunit;

import com.github.springtestdbunit.dataset.AbstractDataSetLoader;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.springframework.core.io.Resource;

import java.io.InputStream;

/**
 * Custom dataset loader for FlatXmlDataSets where the property
 * column sensing has been set to true.
 *
 * @author nikpet
 */
public class CustomFlatXmlDataSetLoader extends AbstractDataSetLoader {

    public CustomFlatXmlDataSetLoader() {

    }

    @Override
    protected IDataSet createDataSet(Resource resource) throws Exception {

        FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();

        builder.setColumnSensing(true);

        InputStream inputStream = resource.getInputStream();
        try {
            return builder.build(inputStream);
        } finally {
            inputStream.close();
        }
    }

}
