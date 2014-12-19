package se.inera.webcert.service.diagnos.repo;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import se.inera.webcert.service.diagnos.model.Diagnos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Map-based repository holding diagnosises.
 *
 * @author npet
 *
 */
public class DiagnosRepositoryImpl implements DiagnosRepository {

    private Map<String, Diagnos> diagnoses = new TreeMap<String, Diagnos>();

    private SortedSet<String> diagnoisCodesSet = new TreeSet<String>();

    private RAMDirectory index = new RAMDirectory();
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;

    /*
     * (non-Javadoc)
     *
     * @see se.inera.webcert.service.diagnos.model.DiagnosRepository#getDiagnosByCode(java.lang.String)
     */
    @Override
    public Diagnos getDiagnosByCode(String code) {
        code = sanitizeCodeValue(code);
        return code != null ? diagnoses.get(code) : null;
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.webcert.service.diagnos.model.DiagnosRepository#searchDiagnosisByCode(java.lang.String)
     */
    @Override
    public List<Diagnos> searchDiagnosisByCode(String codeFragment) {

        List<Diagnos> matches = new ArrayList<Diagnos>();

        String lowVal = sanitizeCodeValue(codeFragment);

        if (lowVal == null) {
            return matches;
        }

        String highVal = createHighValue(lowVal);

        SortedSet<String> keys = diagnoisCodesSet.subSet(lowVal, highVal);

        for (String key : keys) {
            matches.add(diagnoses.get(key));
        }

        return matches;
    }

    public RAMDirectory getLuceneIndex() {
        return index;
    }

    @Override
    public void openLuceneIndexReader() throws IOException {
        indexReader = DirectoryReader.open(index);
        indexSearcher = new IndexSearcher(indexReader);
    }

    @Override
    public List<Diagnos> searchDiagnosisByDescription(String searchString, int nbrOfResults) {
        List<Diagnos> matches = new ArrayList<Diagnos>();

        BooleanQuery query = new BooleanQuery();
        StandardAnalyzer analyzer = new StandardAnalyzer();
        try {
            TokenStream tokenStream = analyzer.tokenStream("description", searchString);
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String term = charTermAttribute.toString();
                query.add(new PrefixQuery(new Term("description", term)), BooleanClause.Occur.MUST);
            }

            if (indexSearcher == null) {
                throw new RuntimeException("Lucene index searcher is not opened");
            }

            TopDocs results = indexSearcher.search(query, nbrOfResults);
            for (ScoreDoc hit : results.scoreDocs) {
                matches.add(diagnoses.get(indexSearcher.doc(hit.doc).get("code")));
            }
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred in lucene index search", e);
        }

        return matches;
    }

    public String sanitizeCodeValue(String codeValue) {

        if (StringUtils.isBlank(codeValue)) {
            return null;
        }

        codeValue = StringUtils.deleteWhitespace(codeValue);
        codeValue = StringUtils.remove(codeValue, '.');

        return (StringUtils.isBlank(codeValue)) ? null : codeValue.toUpperCase();
    }

    public String createHighValue(String lowStr) {
        char[] highCharArray = lowStr.toCharArray();
        highCharArray[highCharArray.length - 1] = ++highCharArray[highCharArray.length - 1];
        return String.valueOf(highCharArray);
    }

    public void addDiagnos(Diagnos diagnos) {
        if (diagnos != null) {
            diagnoisCodesSet.add(diagnos.getKod());
            diagnoses.put(diagnos.getKod(), diagnos);
        }
    }

    public int nbrOfDiagosis() {
        return diagnoses.size();
    }

}
