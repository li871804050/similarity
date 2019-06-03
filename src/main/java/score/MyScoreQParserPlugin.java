package score;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queries.payloads.PayloadFunction;
import org.apache.lucene.queries.payloads.PayloadScoreQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.util.PayloadUtils;

import java.io.IOException;


public class MyScoreQParserPlugin extends QParserPlugin{
    public static final String DEFAULT_OPERATOR = "phrase";
    public static final String DEFAULT_PAYLOAD= "max";
    private PayloadScoreQuery payloadScoreQuery1;
    private PayloadScoreQuery payloadScoreQuery2;
    private PayloadScoreQuery payloadScoreQuery3;
    private String field1 = "click_payload";
    private String field2 = "pay_num_payload";
    private String field3 = "like_num_payload";

    @Override
    public QParser createParser(String s, SolrParams localParams, SolrParams params, SolrQueryRequest solrQueryRequest) {
        String word = params.get("word");
        try {
            this.payloadScoreQuery1= setPayloadScoreQuery(field1, word, solrQueryRequest);
            this.payloadScoreQuery2= setPayloadScoreQuery(field2, word, solrQueryRequest);
            this.payloadScoreQuery3= setPayloadScoreQuery(field3, word, solrQueryRequest);
        } catch (SyntaxError syntaxError) {
            syntaxError.printStackTrace();
        }
        return new MyScoreParser(s, localParams, params, solrQueryRequest, this.payloadScoreQuery1, this.payloadScoreQuery2, this.payloadScoreQuery3);
    }

    /**
     * 内置代码，生成PayloadScoreQuery
     * @param field
     * @param value
     * @param req
     * @return
     * @throws SyntaxError
     */
    public static PayloadScoreQuery setPayloadScoreQuery(String field, String value, SolrQueryRequest req) throws SyntaxError {
        String func = DEFAULT_PAYLOAD;
        String operator = DEFAULT_OPERATOR;
        if (!(operator.equalsIgnoreCase(DEFAULT_OPERATOR) || operator.equalsIgnoreCase("or"))) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Supported operators are : or , phrase");
        }
        boolean includeSpanScore = false;

        if (field == null) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "'f' not specified");
        }

        if (value == null) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "query string missing");
        }

        FieldType ft = req.getCore().getLatestSchema().getFieldType(field);
        Analyzer analyzer = ft.getQueryAnalyzer();
        SpanQuery query;
        try {
            query = PayloadUtils.createSpanQuery(field, value, analyzer, operator);
        } catch (IOException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,e);
        }

        if (query == null) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "SpanQuery is null");
        }

        // note: this query(/parser) does not support func=first; 'first' is a payload() value source feature only
        PayloadFunction payloadFunction = PayloadUtils.getPayloadFunction(func);
        if (payloadFunction == null) {
            throw new SyntaxError("Unknown payload function: " + func);
        }

        return new PayloadScoreQuery(query, payloadFunction, includeSpanScore);
    }
}