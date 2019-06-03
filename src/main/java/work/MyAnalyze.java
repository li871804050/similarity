package work;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyAnalyze {
    public Logger logger = Logger.getLogger("analyze");

    /**
     * 索引分词解析
     *
     * @param analyzer
     * @param query
     * @return
     */
    public List<String> analyzeSentence(Analyzer analyzer, String query) {
        try {
            if (null == query) {
                return new ArrayList<>();
            }
            TokenStream ts = analyzer.tokenStream("someField", query);
            List<String> set = new ArrayList<>();
            CharTermAttribute termAttr = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            while (ts.incrementToken()) {
                String token = termAttr.toString().toLowerCase();
                set.add(token);
            }
            ts.end();
            ts.close();
            return set;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }


    /**
     * 获取query分词结果
     * @param query
     * @param analyzer
     * @return
     * @throws IOException
     */
    public List<String> analyzeQuery(String query, Analyzer analyzer){
        try {
            query = query.replaceAll("\\^[0-9]+(\\.[0-9]+)?", "");
            TokenStream ts = analyzer.tokenStream("someField", query);
            List<String> set = new ArrayList<>();
            CharTermAttribute termAttr = ts.addAttribute(CharTermAttribute.class);
            OffsetAttribute offsetAttribute = ts.addAttribute(OffsetAttribute.class);
            ts.reset();
            int startSet = 0;
            int endSet = 0;
            while (ts.incrementToken()) {
                String token = termAttr.toString().toLowerCase();
                int start = offsetAttribute.startOffset();
                int end = offsetAttribute.endOffset();
                if (startSet == 0 && endSet == 0) {
                    startSet = start;
                    endSet = end;
                    set.add(token);
                } else if (start == startSet && end > endSet) {
                    set.set(set.size() - 1, token);
                    endSet = end;
                }else if (start == startSet && end == endSet && set.get(set.size() - 1).length() < token.length()) {
                    set.set(set.size() - 1, token);
                    endSet = end;
                } else if (start > endSet) {
                    set.add(token);
                    startSet = start;
                    endSet = end;
                }
            }
            ts.end();
            ts.close();
            return set;
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
