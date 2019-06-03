package function;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

import java.util.*;

public class MyValueSourceParser extends ValueSourceParser {


  @Override
  public void init(NamedList namedList) {
  }

  @Override
  public ValueSource parse(FunctionQParser fp) throws SyntaxError {

    List<String> parms = new ArrayList<String>();
    while (true){
      String p = fp.parseArg();
      if (p != null){
        parms.add(p);
      }else {
        break;
      }
    }
    return new MyValueSource(parms);
  }


}