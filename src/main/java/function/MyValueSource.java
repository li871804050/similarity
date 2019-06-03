package function;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.FloatDocValues;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MyValueSource extends ValueSource {
  List<String> parms;

  public MyValueSource(List<String> parms) {
    this.parms = parms;
  }

  @Override
  public FunctionValues getValues(final Map context, final LeafReaderContext reader) throws IOException {
    return new FloatDocValues(this){

      @Override
      public float floatVal(int i) throws IOException {
        LeafReader leafReader = reader.reader();
        Document document = leafReader.document(i);
        return getScoreFromFields(document);
      }
    };
  }

  @Override
  public boolean equals(Object o) {
    return false;
  }
  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public String description() {
    return "myFunction";
  }

  public float getScoreFromFields(Document document){
    int parmCount = parms.size();
    if (parmCount == 1){
      String field = parms.get(0);
      return Float.parseFloat(document.get(field));
    }else if (parmCount == 2){
      String field = parms.get(0);
      String type = parms.get(1);
      String fieldValue = document.get(field);
      String[] values = fieldValue.split(" ");
      for (String value: values){
        if (value.startsWith(type)){
          return Float.parseFloat(value.substring(type.length() + 1));
        }
      }
    }
    return 0.0f;
  }

}