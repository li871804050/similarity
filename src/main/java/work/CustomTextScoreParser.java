package work;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.DisMaxQParser;
import org.apache.solr.search.ExtendedDismaxQParser;
import org.apache.solr.search.SyntaxError;

import java.io.IOException;
import java.util.*;

public class CustomTextScoreParser extends ExtendedDismaxQParser {
    private Logger logger = Logger.getLogger(CustomTextScoreParser.class);
    private Map<String, Set<String>> synonymsMap;
    private String querys;


    CustomTextScoreParser(String qstr, SolrParams localParams, SolrParams solrparams, SolrQueryRequest req,
                          Map<String, Set<String>> synonyms) {
        super(qstr, localParams, solrparams, req);
        this.synonymsMap = synonyms;
        querys = qstr;
    }


    @Override
    public Query parse() throws SyntaxError {
        try {
            String qf = this.params.get(Constant.QF);
            if (qf == null) {
                throw new SyntaxError("no qf specified in solr params");
            }
            MyAnalyze bgAnalyze = new MyAnalyze();
            IndexSchema schema = this.req.getSchema();
            Map<String, Float> qfMap = DisMaxQParser.parseQueryFields(schema, this.params);
            FieldType fieldType = getFieldType(qfMap.keySet());
            Analyzer queryAnalyzer = fieldType.getQueryAnalyzer();
            List<String> queryList = bgAnalyze.analyzeQuery(querys, queryAnalyzer);
            Set<String> queryTokens = new HashSet<>(queryList);
            Map<String, Boost> boostMap = Boost.addBoost(this.params.get(Constant.BOOST));
            String bf = this.params.get(Constant.BF);
            if (null != bf) {
                bf = bf.replace("max(if(exists(query({!v=cat_path:", "");
                bf = bf.replace("})),100,1))", "");
            }
            Map<String, Set<String>> querySynonyms = parseQuerySynonyms(queryTokens);
            Map<String, Float> boostParam = parseQueryBoost(Utils.parseQueryBoosts(querys), querySynonyms);
            String similarity = this.params.get(Constant.SIMILARITY);
//            String op = Constant.OR;
//            if (null != this.params.get(Constant.OP)) {
//                op = this.params.get(Constant.OP);
//            }
//            int mm = 1;
//            String mStr = this.params.get(Constant.MM);
//            if (null != mStr) {
//                if (mStr.endsWith("%")) {
//                    mm = (int) Math.floor(queryList.size() * Float.parseFloat(mStr.substring(0, mStr.length() - 1)) / 100);
//                } else {
//                    mm = Integer.parseInt(mStr);
//                }
//            }
//            if (Constant.AND.equals(op) || "and".equals(op)) {
//                mm = queryList.size();
//            }
//            Query query2 = filterQuery(querySynonyms, qfMap.keySet(), mm);
            Query query2 = super.parse();
            return new CustomTextScoreQuery(query2, qstr, qfMap, boostParam, querySynonyms, similarity, boostMap, bf);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new SyntaxError(e);
        }
    }


    /**
     * 获取同义词
     *
     * @param queryTokens
     * @return
     */
    private Map<String, Set<String>> parseQuerySynonyms(Set<String> queryTokens) {
        Map<String, Set<String>> querySynonyms = new HashMap<>();
        for (String tok : queryTokens) {
            Set<String> set = this.synonymsMap.getOrDefault(tok, new HashSet<>());
            set.add(tok);
            querySynonyms.put(tok, set);
        }
        return querySynonyms;
    }

    /**
     * 词的权重
     *
     * @param queryBoost
     * @param querySynonyms
     * @return
     * @throws IOException
     */
    private Map<String, Float> parseQueryBoost(Map<String, Float> queryBoost, Map<String, Set<String>> querySynonyms) throws IOException {
        Map<String, Float> boostParam = new HashMap<>();
        for (String key : querySynonyms.keySet()) {
            Set<String> set = querySynonyms.get(key);
            boolean find = false;
            for (String s : set) {
                if (queryBoost.containsKey(s)) {
                    boostParam.put(key, queryBoost.get(s));
                    find = true;
                    break;
                }
            }
            if (!find) {
                for (String s : set) {
                    if (s.contains(" ")) {
                        String[] words = s.split(" ");
                        for (String w : words) {
                            if (queryBoost.containsKey(w)) {
                                boostParam.put(key, queryBoost.get(w));
                                find = true;
                                break;
                            }
                        }
                    }
                    if (find) {
                        break;
                    }
                }
            }
        }
        return boostParam;
    }

    /**
     * 获取查询字段类型
     *
     * @param qfSet
     * @return
     */
    private FieldType getFieldType(Set<String> qfSet) {
        String[] qfParams = new String[qfSet.size()];
        int i = 0;
        for (String qf : qfSet) {
            qfParams[i] = qf;
            i = i + 1;
        }
        IndexSchema schema = this.req.getSchema();
        boolean allEquals = Arrays.stream(qfParams).map(schema::getFieldType).distinct().count() == 1;
        if (allEquals) {
            return schema.getFieldType(qfParams[0]);
        }
        throw new RuntimeException("qf contains different field type!");
    }


    /**
     * 过滤条件组合
     *
     * @param map
     * @param qfSet
     * @param mm
     * @return
     */
    private Query filterQuery(Map<String, Set<String>> map, Set<String> qfSet, int mm) {
//        logger.info("条件组合开始");
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        BooleanClause.Occur occur = BooleanClause.Occur.SHOULD;
        for (String qf : qfSet) {
            BooleanQuery.Builder builder1 = new BooleanQuery.Builder();
            for (Set<String> words : map.values()) {
                BooleanQuery.Builder builder2 = new BooleanQuery.Builder();
                for (String word : words) {
                    if (word.contains(" ")) {
                        String[] strings = word.split(" ");
                        BooleanQuery.Builder builder3 = new BooleanQuery.Builder();
                        for (String s : strings) {
                            Term term = new Term(qf, s);
                            Query query = new TermQuery(term);
                            builder3.add(query, BooleanClause.Occur.MUST);
                        }
                        builder2.add(builder3.build(), BooleanClause.Occur.SHOULD);
                    } else {
                        Term term = new Term(qf, word);
                        Query query = new TermQuery(term);
                        builder2.add(query, BooleanClause.Occur.SHOULD);
                    }
                }
                builder1.add(builder2.build(), occur);
            }
            builder1.setMinimumNumberShouldMatch(mm);
            builder.add(builder1.build(), BooleanClause.Occur.SHOULD);
        }
//        logger.info("条件组合结束");
        return builder.build();
    }

}