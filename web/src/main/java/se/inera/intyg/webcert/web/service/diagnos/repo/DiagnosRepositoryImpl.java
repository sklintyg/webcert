/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.diagnos.repo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.RAMDirectory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;

/**
 * Map-based repository holding diagnosises.
 *
 * @author npet
 *
 */
public class DiagnosRepositoryImpl implements DiagnosRepository {

    private final RAMDirectory index = new RAMDirectory();
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;

    @Override
    public List<Diagnos> getDiagnosesByCode(String code) {
        String codeSanitized = sanitizeCodeValue(code);
        if (codeSanitized == null) {
            return new ArrayList<>();
        }
        try {
            int freq = indexReader.docFreq(new Term(CODE, codeSanitized));
            TermQuery query = new TermQuery(new Term(CODE, codeSanitized));
            return searchDiagnosisByQuery(query, Math.max(1, freq));
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred in lucene index search", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.web.service.diagnos.model.DiagnosRepository#searchDiagnosisByCode(java.lang.String)
     */
    @Override
    public List<Diagnos> searchDiagnosisByCode(String codeFragment, int nbrOfResults) {
        String codeFragmentSanitized = sanitizeCodeValue(codeFragment);
        if (codeFragmentSanitized == null) {
            return new ArrayList<>();
        }
        PrefixQuery query = new PrefixQuery(new Term(CODE, codeFragmentSanitized));
        return searchDiagnosisByQuery(query, nbrOfResults);
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
        if (Strings.isNullOrEmpty(searchString)) {
            return Collections.emptyList();
        }
        BooleanQuery.Builder query = new BooleanQuery.Builder();
        try (StandardAnalyzer analyzer = new StandardAnalyzer()) {
            TokenStream tokenStream = analyzer.tokenStream(DESC, searchString);
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String term = WildcardQuery.WILDCARD_STRING + charTermAttribute.toString() + WildcardQuery.WILDCARD_STRING;
                query.add(new WildcardQuery(new Term(DESC, term)), BooleanClause.Occur.MUST);
            }
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred in lucene index search", e);
        }
        return searchDiagnosisByQuery(query.build(), nbrOfResults);
    }

    private List<Diagnos> searchDiagnosisByQuery(Query query, int nbrOfResults) {
        List<Diagnos> matches = new ArrayList<>();

        try {
            if (indexSearcher == null) {
                throw new RuntimeException("Lucene index searcher is not opened");
            }

            TopDocs results = indexSearcher.search(query, nbrOfResults);
            for (ScoreDoc hit : results.scoreDocs) {
                Diagnos d = new Diagnos();
                d.setKod(indexSearcher.doc(hit.doc).get(CODE).toUpperCase());
                d.setBeskrivning(indexSearcher.doc(hit.doc).get(DESC));
                matches.add(d);
            }
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred in lucene index search", e);
        }

        return matches;
    }

    public String sanitizeCodeValue(String codeValueParam) {
        String codeValue = Strings.nullToEmpty(codeValueParam);
        codeValue = CharMatcher.is('.').or(CharMatcher.whitespace()).removeFrom(codeValue);

        return codeValue.trim().isEmpty() ? null : codeValue.toUpperCase();
    }

    public int nbrOfDiagosis() {
        return indexReader.numDocs();
    }

}
