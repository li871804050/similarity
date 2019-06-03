package work;

import org.apache.log4j.Logger;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

public class CustomTextScoreProvider extends CustomScoreProvider {

    private String similarity;
    private Map<Integer, Float> resultScores;
    public Logger logger = Logger.getLogger(CustomTextScoreProvider.class);

    CustomTextScoreProvider(LeafReaderContext context, Map<Integer, Float> resultScores, String similarity){
        super(context);
        this.resultScores = resultScores;
        this.similarity = similarity;
    }

    @Override
    public float customScore(int doc, float subQueryScore, float valSrcScore) throws IOException {
        if (null != this.similarity && (this.similarity.equals(Constant.AI_SYNONYM) || this.similarity.equals(Constant.TEXT_SYNONYM))) {
            DecimalFormat decimalFormat = new DecimalFormat("0.0000");
            return Float.parseFloat(decimalFormat.format(this.resultScores.getOrDefault(doc, 0.0f)));
//            return this.resultScores.getOrDefault(doc, 0.0f);
        } else {
            return 0.0f;
        }
    }

    public float customScore(int doc) throws IOException {
        if (null != this.similarity && (this.similarity.equals(Constant.AI_SYNONYM) || this.similarity.equals(Constant.TEXT_SYNONYM))) {
            DecimalFormat decimalFormat = new DecimalFormat("0.0000");
            logger.info("end\t" + System.currentTimeMillis());
            return Float.parseFloat(decimalFormat.format(this.resultScores.getOrDefault(doc, 0.0f)));
//            return this.resultScores.getOrDefault(doc, 0.0f);
        } else {
            return 0.0f;
        }
    }
}
