package filter;

import org.apache.lucene.analysis.CharacterUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;

public class MyStemFilter extends TokenFilter {
    //需要进行处理的数据格式
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);

    public MyStemFilter(TokenStream input){
        super(input);

    }


    @Override
    public final boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            CharacterUtils.toLowerCase(termAtt.buffer(), 0, termAtt.length());
            return true;
        } else {
            return false;
        }
    }
}
