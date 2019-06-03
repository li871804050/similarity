package analyze;


import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;

public class MyTokenizer extends Tokenizer {
    public static final int DEFAULT_BUFFER_SIZE = 256;
    private boolean done = false;
    private int finalOffset;
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    public MyTokenizer() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public MyTokenizer(int bufferSize) {
        if (bufferSize > MyTokenizerFactory.MAX_TOKEN_LENGTH_LIMIT || bufferSize <= 0) {
            throw new IllegalArgumentException("maxTokenLen must be greater than 0 and less than " + MyTokenizerFactory.MAX_TOKEN_LENGTH_LIMIT + " passed: " + bufferSize);
        }
        termAtt.resizeBuffer(bufferSize);
    }

    public MyTokenizer(AttributeFactory factory, int bufferSize){
        super(factory);
        if (bufferSize > MyTokenizerFactory.MAX_TOKEN_LENGTH_LIMIT || bufferSize <= 0) {
            throw new IllegalArgumentException("maxTokenLen must be greater than 0 and less than " + MyTokenizerFactory.MAX_TOKEN_LENGTH_LIMIT + " passed: " + bufferSize);
        }
        termAtt.resizeBuffer(bufferSize);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!done) {
            clearAttributes();
            done = true;
            int upto = 0;
            char[] buffer = termAtt.buffer();
            while (true) {
                final int length = input.read(buffer, upto, buffer.length-upto);
                if (length == -1) {
                    break;
                }
                upto += length;
                if (upto == buffer.length) {
                    buffer = termAtt.resizeBuffer(1 + buffer.length);
                }
            }
            termAtt.setLength(upto);
            finalOffset = correctOffset(upto);
            offsetAtt.setOffset(correctOffset(0), finalOffset);
            return true;
        }
        return false;
    }
}
