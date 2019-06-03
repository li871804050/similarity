package work;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;

import java.io.IOException;
import java.util.*;

public class Scores {

    public static Logger logger = Logger.getLogger(Scores.class);
    /**
     * 获取doc对应得分
     * @return
     */
    public static Map<Integer, Float> getScores(Map<String, Set<String>> querySynonyms, Map<String, Float> boost,
                                                Map<String, Float> qfParams, LeafReader reader, Map<String, Boost> boostMap, String bf) {

        Map<Integer, Float> res = getTextScores(querySynonyms, boost, qfParams, reader);
        for (Integer i : res.keySet()) {
            float f = getScoreBG(reader, i, res.get(i), boostMap, bf);
            res.put(i, f);
        }
        return res;
    }

    /**
     * 获取doc对应得分
     * @return
     */
    public static Map<Integer, Float> getTextScores(Map<String, Set<String>> querySynonyms, Map<String, Float> boost,
                                                Map<String, Float> qfParams, LeafReader reader) {
        Map<Integer, Float> res = new HashMap<>();
        for (String word: querySynonyms.keySet()){
            float boostWord = boost.getOrDefault(word, 1.0f);
            Map<Integer, Float> wordMap = new HashMap<>();
            for (String qf: qfParams.keySet()){
                float boots = qfParams.getOrDefault(qf, 1.0f)==null? 1.0f : qfParams.getOrDefault(qf, 1.0f);
                Set<String> syns = querySynonyms.get(word);
                for (String syn: syns){
                    if (syn.startsWith("##")){
                        continue;
                    }
                    if (syn.contains(" ")){
                        Map<Integer, Float> mapT = getDocIds2(qf, syn, reader);
                        wordMap = addMap(wordMap, mapT, boots);
                    }else {
                        Set<Integer> set = getDocIds(qf, syn, reader);
                        wordMap = addSet(wordMap, set, boots);
                    }
                }
            }
            for (Integer i: wordMap.keySet()){
                float sc = res.getOrDefault(i, 0.0f);
                sc += wordMap.get(i)*boostWord;
                res.put(i, sc);
            }
        }

        for (Integer i: res.keySet()){
            float score = (float) Math.sqrt(res.get(i));
            res.put(i, score);
        }
        return res;
    }


    private static float getScoreBG(LeafReader reader, Integer i, float score, Map<String, Boost> boostMap, String bf) {
        float n = 0f;
        float p = 0f;
        float d = 0f;
        float orderScore = 0f;
        float bfScore = 0;

        int a = 1000;
        float b = 0.3f;
        float s = 0.00001f;

        try {
            Document document = reader.document(i);
            String prodctsId = document.get(Constant.PRODUCTS_ID);
            if (Constant.F.equals(document.get(Constant.STOCKS))){
                s = 1;
            }
            orderScore = Float.parseFloat(document.get(Constant.ORDER_SCORE));
            String bfSolr = document.get(Constant.CUT_PATH);
            if (bf != null && null != bfSolr && bfSolr.contains(bf)){
                bfScore = 100;
            }
            if (null != boostMap && boostMap.containsKey(prodctsId)) {
                Boost boost = boostMap.getOrDefault(prodctsId, null);
                if (boost != null) {
                    n = boost.getUV();
                    p = boost.getOrderUV();
                    d = boost.getOrderAvg();
                }
            }
        }catch (IOException e){
            logger.info(e.getMessage(), e);
        }

        logger.info("文本分:" + score + "\tn:" + n + "\tp:" + p + "\td:" + d
                + "\ts:" + s + "\tw:" + orderScore + "\tbfScore:" + bfScore);

        if (p > 1) {
            p = 1f;
        }

        if (n == 0 || p == 0 || d == 0) {
            float f = (float) ((b * Math.pow(orderScore, 1.0 / 3.0) + 1) * 10);
//            logger.info("f:" + f);
//            logger.info("公式：score * s * f + bfScore");
            score = score * s * f + bfScore;
//            logger.info("总得分:" + score);
        }else {
            float k = (float) ((p + 1.96 * 1.96 / (2 * n)
                    - 1.96 * Math.sqrt((p * (1 - p) / n) + 1.96 * 1.96 / (4 * n * n))) / (1 + 1.96 * 1.96 / n));
//            logger.info("k:" + k);
            float z = a * d * k;
//            logger.info("z = 1000 * d * k\t" + z);
            float f = (float) ((b * Math.pow(orderScore, 1.0 / 3.0) + 1) * 10);
//            logger.info("f = ((0.3 * Math.pow(orderScore, 1.0 / 3.0) + 1) * 10)\t" + f);
            float ya = z + f;
//            logger.info("ya = z + f\t" + ya  + "=" + z + "+" + f);
            score = score * ya * s + bfScore;
//            logger.info("总得分=score * ya * s + bfScore\t" + score);
        }
        logger.info("总得分:" + score);
        return score;
    }


    private  static Map<Integer,Float> addMap(Map<Integer,Float> wordMap, Map<Integer,Float> mapT, float boots) {
        for (Integer i: mapT.keySet()){
            if (wordMap.getOrDefault(i, 0.0f) < mapT.get(i)*boots){
                wordMap.put(i, mapT.get(i)*boots);
            }
        }
        return wordMap;
    }

    private  static Map<Integer,Float> addSet(Map<Integer,Float> wordMap, Set<Integer> set, float boots) {
        for (Integer s: set){
            if (wordMap.getOrDefault(s, 0.0f) < boots) {
                wordMap.put(s, boots);
            }
        }
        return wordMap;
    }

    private  static Set<Integer> getDocIds(String qf, String word, LeafReader reader){
        try {
            Set<Integer> ids = new HashSet<>();
            Term term = new Term(qf, word);
            PostingsEnum postingsEnum = reader.postings(term);
            while (postingsEnum != null) {
                int docID = postingsEnum.nextDoc();
                if (docID == PostingsEnum.NO_MORE_DOCS) {
                    break;
                }else {
                    ids.add(docID);
                }
            }
            return ids;
        }catch (IOException e){
            logger.info(e.getMessage(), e);
        }
        return null;
    }


    /**
     * 获取短语包含文档id
     * @param qf
     * @param word
     * @return
     */
    private  static Map<Integer, Float> getDocIds2(String qf, String word, LeafReader reader) {
        try {
            String[] words = word.split(" ");
            Map<Integer, Float> floatMap = new HashMap<>();
            Map<Integer, List<Integer>> map = new HashMap<>();
            Term term = new Term(qf, words[0]);
            PostingsEnum postingsEnum = reader.postings(term, PostingsEnum.POSITIONS);
            while (postingsEnum != null) {
                int docID = postingsEnum.docID();
                for (int i=0, len = postingsEnum.freq(); i<len; i++) {
                    int pos = postingsEnum.nextPosition();
                    List<Integer> list = map.getOrDefault(docID, new ArrayList<>());
                    list.add(pos);
                    map.put(docID, list);
                }
                if (postingsEnum.nextDoc() == PostingsEnum.NO_MORE_DOCS) {
                    break;
                }
            }
            for (int i = 1; i < words.length; i++) {
                term = new Term(qf, words[i]);
                postingsEnum = reader.postings(term, PostingsEnum.POSITIONS);
                Map<Integer, List<Integer>> mapStart = new HashMap<>();
                while (postingsEnum != null) {
                    int docID = postingsEnum.nextDoc();
                    if (docID == PostingsEnum.NO_MORE_DOCS) {
                        break;
                    } else {
                        for (int j=0, len = postingsEnum.freq(); j<len; j++) {
                            int startOffset = postingsEnum.nextPosition();
                            List<Integer> list = mapStart.getOrDefault(docID, new ArrayList<>());
                            list.add(startOffset);
                            mapStart.put(docID, list);
                        }
                    }
                }
                map = phraseSc(map, mapStart);
            }
            for (Integer m: map.keySet()){
                floatMap.put(m, 1.0f);
            }
            return floatMap;
        }catch (IOException e){
            logger.info(e.getMessage(), e);
        }
        return null;
    }

    private static  Map<Integer,List<Integer>> phraseSc(Map<Integer,List<Integer>> map, Map<Integer,List<Integer>> mapStart) {
        Map<Integer, List<Integer>> res = new HashMap<>();
        for (Integer i: mapStart.keySet()){
            if (map.containsKey(i)){
                List<Integer> first = map.get(i);
                List<Integer> second = mapStart.get(i);
                if (border(first, second)){
                    res.put(i, mapStart.get(i));
                }
            }
        }
        return res;
    }

    private  static boolean border(List<Integer> first, List<Integer> second) {
        int dis = 100;
        for (int f: first){
            for (int s: second){
                int d = s - f;
                if (d == 1){
                    dis = d;
                    break;
                }
            }
        }
        if (dis == 1){
            return true;
        }
        return false;
    }

}
