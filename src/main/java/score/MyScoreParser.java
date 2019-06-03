package score;

import org.apache.lucene.queries.payloads.PayloadScoreQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.ExtendedDismaxQParser;
import org.apache.solr.search.SyntaxError;

public class MyScoreParser extends ExtendedDismaxQParser {
    String catIds;
    private PayloadScoreQuery payloadScoreQuery1;
    private PayloadScoreQuery payloadScoreQuery2;
    private PayloadScoreQuery payloadScoreQuery3;

    MyScoreParser(String qstr, SolrParams localParams, SolrParams solrparams, SolrQueryRequest req,
                  PayloadScoreQuery payloadScoreQuery1, PayloadScoreQuery payloadScoreQuery2, PayloadScoreQuery payloadScoreQuery3) {
        super(qstr, localParams, solrparams, req);
        this.catIds = solrparams.get("cates");
        this.payloadScoreQuery1 = payloadScoreQuery1;
        this.payloadScoreQuery2 = payloadScoreQuery2;
        this.payloadScoreQuery3 = payloadScoreQuery3;
    }


    @Override
    public Query parse() throws SyntaxError {
            Query query = super.parse();
            return new MyScoreQuery(query, catIds, payloadScoreQuery1, payloadScoreQuery2, payloadScoreQuery3);
    }
}