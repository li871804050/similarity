package score;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.QueryValueSource;
import org.apache.lucene.queries.payloads.PayloadScoreQuery;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.*;

public class MyScoreQuery extends CustomScoreQuery {



    private Query query;
    private String catsId;

    private String field4 = "order_score";
    private String feild5 = "cat_path";
    private PayloadScoreQuery payloadScoreQuery1;
    private PayloadScoreQuery payloadScoreQuery2;
    private PayloadScoreQuery payloadScoreQuery3;
    public static Logger logger = Logger.getLogger(CustomScoreQuery.class);

    MyScoreQuery(Query query, String catIds, PayloadScoreQuery payloadScoreQuery1,
                 PayloadScoreQuery payloadScoreQuery2, PayloadScoreQuery payloadScoreQuery3) {
        super(query);
        this.query = query;
        this.catsId = catIds;
        this.payloadScoreQuery1 = payloadScoreQuery1;
        this.payloadScoreQuery2 = payloadScoreQuery2;
        this.payloadScoreQuery3 = payloadScoreQuery3;

    }



    //=========================== W E I G H T ============================

    private class AiCustomWeight extends Weight {
        final Weight subQueryWeight;
        final float queryWeight;
        private IndexSearcher searcher;

        public AiCustomWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
            super(MyScoreQuery.this);
            this.searcher = searcher;
            this.queryWeight = boost;
            this.subQueryWeight = query.createWeight(searcher, needsScores, 1f);

        }

        @Override
        public void extractTerms(Set<Term> terms) {

        }

        @Override
        public Scorer scorer(LeafReaderContext context) throws IOException {
            try {
                Scorer subQueryScorer = subQueryWeight.scorer(context);
                if (null == subQueryScorer){
                    return null;
                }
                QueryValueSource vs = new QueryValueSource(payloadScoreQuery1, 0.0f);
                FunctionValues functionValues1 = vs.getValues(ValueSource.newContext(this.searcher), context);
                vs = new QueryValueSource(payloadScoreQuery2, 0.0f);
                FunctionValues functionValues2 = vs.getValues(ValueSource.newContext(this.searcher), context);
                vs = new QueryValueSource(payloadScoreQuery3, 0.0f);
                FunctionValues functionValues3 = vs.getValues(ValueSource.newContext(this.searcher), context);
                return new AiCustomScorer(MyScoreQuery.this, subQueryScorer, context.reader(), functionValues1,
                        functionValues2, functionValues3);
            }catch (Exception e){
                logger.error(e.getMessage(), e);
                return null;
            }
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
        private final MyScoreQuery provider;
        private final LeafReader reader;
        private FunctionValues functionValues1;
        private FunctionValues functionValues2;
        private FunctionValues functionValues3;

        // constructor
        private AiCustomScorer(MyScoreQuery provider, Scorer subQueryScorer, LeafReader reader, FunctionValues functionValues1,
                               FunctionValues functionValues2, FunctionValues functionValues3) {
            super(subQueryScorer);
            this.subQueryScorer = subQueryScorer;
            this.provider = provider;
            this.reader = reader;
            this.functionValues1 = functionValues1;
            this.functionValues2 = functionValues2;
            this.functionValues3 = functionValues3;
            reader.getContext();


        }

        @Override
        public float score() throws IOException {
            int id = subQueryScorer.docID();
            Document document = reader.document(id);
            float score = subQueryScorer.score();
            float score0 = getCatIdsScore(document, provider.feild5, provider.catsId);

            float score1 = functionValues1.floatVal(id);
            float score2 = functionValues2.floatVal(id);
            float  score3 = functionValues3.floatVal(id);

            String value4 = document.get(provider.field4);
            float score4 = 0.0f;
            if (null != value4){
                score4 = Float.parseFloat(document.get(provider.field4));
            }
            return score*400 + score0*3000 + score1*300 + score2*30 + score3*10 + score4*3;
        }

        private float getCatIdsScore(Document document, String feild5, String catsId) {
            String cs = document.get(feild5);
            if (null != cs) {
                String[] cats = cs.split("-");
                List<String> catsIds = Arrays.asList(catsId.split(","));
                for (String cat : cats) {
                    if (catsIds.contains(cat)) {
                        return 1.0f;
                    }
                }
            }
            return 0.0f;
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
