/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.infra.xmldsig.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

public final class XsltUtil {

  private XsltUtil() {}

  private static final Logger LOG = LoggerFactory.getLogger(XsltUtil.class);

  public static void transform(InputStream inXml, OutputStream outXml, String xsltFile) {
    // CHECKSTYLE:OFF EmptyCatchBlock
    try {
      ClassPathResource cpr = new ClassPathResource(xsltFile);

      // Create transformer factory
      TransformerFactory factory = TransformerFactory.newInstance();

      // Use the factory to create a template containing the xsl file
      Templates template = factory.newTemplates(new StreamSource(cpr.getInputStream()));

      // Use the template to create a transformer
      Transformer xformer = template.newTransformer();

      // Prepare the input and output files
      Source source = new StreamSource(inXml);
      Result result = new StreamResult(outXml);

      // Apply the xsl file to the source file and write the result
      // to the output file
      xformer.transform(source, result);
    } catch (FileNotFoundException | TransformerConfigurationException e) {
    } catch (IOException e) {
      LOG.error("XSLT transformer IOException: {}", e.getMessage());
    } catch (TransformerException e) {
      // An error occurred while applying the XSL file
      // Get location of error in input file
      SourceLocator locator = e.getLocator();
      int col = locator.getColumnNumber();
      int line = locator.getLineNumber();
      String publicId = locator.getPublicId();
      String systemId = locator.getSystemId();

      LOG.error("XSLT transformer exception: {}", e.getMessage());
      LOG.error(
          "Details: line: {} col: {} publicId: {} systemId: {}", line, col, publicId, systemId);
    }
    // CHECKSTYLE:ON EmptyCatchBlock
  }
}
