package se.inera.intyg.webcert.web.test;

import static org.custommonkey.xmlunit.DifferenceConstants.NAMESPACE_PREFIX_ID;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

/**
 * @author andreaskaltenbach
 */
public class NamespacePrefixNameIgnoringListener implements DifferenceListener {

    @Override
    public int differenceFound(Difference difference) {
        if (NAMESPACE_PREFIX_ID == difference.getId()) {
            // differences in namespace prefix IDs are ok (eg. 'ns1' vs 'ns2'), as long as the namespace URI is the same
            return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
        } else {
            return RETURN_ACCEPT_DIFFERENCE;
        }
    }

    @Override
    public void skippedComparison(Node control, Node test) {
    }
}
