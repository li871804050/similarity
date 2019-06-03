package similarity;

import org.apache.lucene.search.similarities.Similarity;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.schema.SimilarityFactory;

public class MySimilarityFactory extends SimilarityFactory {

  @Override
  public void init(SolrParams params) {
    super.init(params);
  }
  @Override
  public Similarity getSimilarity() {
    MyTFIDFSimilarity tfidfSimilarity = new MyTFIDFSimilarity();
    MyBM25Similarity bm25Similarity = new MyBM25Similarity();
    if (params.get("similarity").equals("TFIDF")) {
      return tfidfSimilarity;
    }else {
      return bm25Similarity;
    }
  }
}