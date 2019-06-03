package work;

import org.apache.log4j.Logger;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.*;

public class CustomTextScoreQuery extends CustomScoreQuery {

    private Map<String, Float> qfParams;
    private Map<String, Float> boost;
    private Map<String, Set<String>> querySynonyms;
    private String similarity;
    private String qstr;
    private Query query;
    private Map<Integer, Float> res;
    private Map<String, Boost> boostMap;
    private String bf;
    public static Logger logger = Logger.getLogger(CustomScoreQuery.class);

    CustomTextScoreQuery(Query query, String qstr, Map<String, Float> qfParams, Map<String, Float> boost,
                         Map<String, Set<String>> querySynonyms, String similarity, Map<String, Boost> boostMap, String bf) {
        super(query);
        this.query = query;
        this.qfParams = qfParams;
        this.similarity = similarity;
        if (null != boost) {
            this.boost = boost;
        }else {
            Map<String, Float> map = new HashMap<>();
            map.put(" ", 1.0f);
            this.boost = boost;
        }
        this.qstr = qstr;
        this.querySynonyms = querySynonyms;
        this.boostMap = boostMap;
        this.bf = bf;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof CustomTextScoreQuery) {
            CustomTextScoreQuery otherQuery = (CustomTextScoreQuery) other;
            if (super.equals(otherQuery)) {
                if (this.qstr.equals(otherQuery.qstr)) {
                    if (this.similarity != null) {
                        return this.similarity.equals(otherQuery.similarity);
                    } else {
                        return otherQuery.similarity == null;
                    }
                }
            }
        }
        return false;
    }


    @Override
    public int hashCode() {
        if (this.similarity != null) {
            return super.hashCode() + this.similarity.hashCode() + this.qstr.hashCode();
        } else {
            return super.hashCode();
        }
    }


    //=========================== W E I G H T ============================

    private class AiCustomWeight extends Weight {
        final Weight subQueryWeight;
        final float queryWeight;

        public AiCustomWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
            super(CustomTextScoreQuery.this);
            this.subQueryWeight = query.createWeight(searcher, needsScores, 1f);
            this.queryWeight = boost;
        }

        @Override
        public void extractTerms(Set<Term> terms) {

        }

        @Override
        public Scorer scorer(LeafReaderContext context) throws IOException {
            Scorer subQueryScorer = subQueryWeight.scorer(context);
            if (null == subQueryScorer){
                return null;
            }
            if (Constant.AI_SYNONYM.equals(CustomTextScoreQuery.this.similarity)) {
                CustomTextScoreQuery.this.res = Scores.getScores(querySynonyms, boost, qfParams, context.reader(), boostMap, bf);
            }else if (Constant.TEXT_SYNONYM.equals(CustomTextScoreQuery.this.similarity)) {
                CustomTextScoreQuery.this.res = Scores.getTextScores(querySynonyms, boost, qfParams, context.reader());
            }
            return new AiCustomScorer(CustomTextScoreQuery.this, subQueryScorer);
        }

        @Override
        public Explanation explain(LeafReaderContext context, int doc) throws IOException {
            Explanation explain = doExplain(context, doc);
            return explain == null ? Explanation.noMatch("no matching docs") : explain;
        }

        private Explanation doExplain(LeafReaderContext info, int doc) throws IOException {
            return null;
        }

    }




    //=========================== S C O R E R ============================

    /**
     * A scorer that applies a (callback) function on scores of the subQuery.
     */
    private static class AiCustomScorer extends FilterScorer {
        private final Scorer subQueryScorer;
        private final CustomTextScoreQuery provider;

        // constructor
        private AiCustomScorer(CustomTextScoreQuery provider, Scorer subQueryScorer) {
            super(subQueryScorer);
            this.subQueryScorer = subQueryScorer;
            this.provider = provider;
        }

        @Override
        public float score() throws IOException {
            float c = provider.res.getOrDefault(subQueryScorer.docID(), 0.0f);
            return c;
        }

        @Override
        public Collection<ChildScorer> getChildren() {
            return Collections.singleton(new ChildScorer(subQueryScorer, "CUSTOM"));
        }
    }

    @Override
    public String name() {
        return "AiCustom";
    }

    @Override
    public Weight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
        return new AiCustomWeight(searcher, needsScores, boost);
    }

}
