package query;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.xml.ParserException;
import org.apache.lucene.queryparser.xml.QueryBuilder;
import org.apache.lucene.queryparser.xml.builders.SpanQueryBuilder;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.SolrQueryBuilder;
import org.apache.solr.search.SolrSpanQueryBuilder;
import org.w3c.dom.Element;

public class MySolrQueryBuilder extends SolrSpanQueryBuilder {


    public MySolrQueryBuilder(String defaultField, Analyzer analyzer, SolrQueryRequest req, SpanQueryBuilder spanFactory) {
        super(defaultField, analyzer, req, spanFactory);
    }

    @Override
    public Query getQuery(Element e) throws ParserException {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        Query query = getSpanQuery(e);
        return query;
    }

    @Override
    public SpanQuery getSpanQuery(Element e) throws ParserException {
        return null;
    }
}
