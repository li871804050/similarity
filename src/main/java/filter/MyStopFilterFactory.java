package filter;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.io.IOException;
import java.util.Map;

public class MyStopFilterFactory extends TokenFilterFactory{
  public static final String FORMAT_WORDSET = "wordset";
  public static final String FORMAT_SNOWBALL = "snowball";

  private CharArraySet stopWords;
  private final String stopWordFiles;
  private final String format;
  private final boolean ignoreCase;
  
  /** Creates a new StopFilterFactory */
  public MyStopFilterFactory(Map<String,String> args) {
    super(args);
    stopWordFiles = get(args, "words");
    format = get(args, "format", (null == stopWordFiles ? null : FORMAT_WORDSET));
    ignoreCase = getBoolean(args, "ignoreCase", false);
    if (!args.isEmpty()) {
      throw new IllegalArgumentException("Unknown parameters: " + args);
    }
  }

  public boolean isIgnoreCase() {
    return ignoreCase;
  }


  @Override
  public TokenStream create(TokenStream input) {
    MyStemFilter stopFilter = new MyStemFilter(input);
    return stopFilter;
  }
}
