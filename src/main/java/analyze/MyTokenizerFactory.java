package analyze;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

import java.util.Map;

public class MyTokenizerFactory extends TokenizerFactory {
    public static final int MAX_TOKEN_LENGTH_LIMIT = 1024 * 1024;
    int maxTokenLen;

    /**
     * Initialize this factory via a set of key-value pairs.
     *
     * @param args
     */
    protected MyTokenizerFactory(Map<String, String> args) {
        super(args);
        maxTokenLen = getInt(args, "maxTokenLen", MyTokenizer.DEFAULT_BUFFER_SIZE);
        if (maxTokenLen > MAX_TOKEN_LENGTH_LIMIT || maxTokenLen <= 0) {
            throw new IllegalArgumentException("maxTokenLen must be greater than 0 and less than " + MAX_TOKEN_LENGTH_LIMIT + " passed: " + maxTokenLen);
        }
        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }

    @Override
    public Tokenizer create(AttributeFactory factory) {
        return new MyTokenizer(factory, maxTokenLen);
    }
}
