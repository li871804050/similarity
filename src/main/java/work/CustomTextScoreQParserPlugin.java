package work;

import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;


public class CustomTextScoreQParserPlugin extends QParserPlugin implements ResourceLoaderAware {

    private List<Set<String>> synonyms;
    private Map<String, Set<String>> synonymsMap;
    private Map<String, Set<String>> mapSpace = new HashMap<>();
    private Map<String, String> mapBlank = new HashMap<>();

    @Override
    public void init(NamedList args) {
    }

    @Override
    public QParser createParser(String s, SolrParams localParams, SolrParams params, SolrQueryRequest solrQueryRequest) {
        return new CustomTextScoreParser(s, localParams, params, solrQueryRequest, this.synonymsMap);
    }


    @Override
    public void inform(ResourceLoader resourceLoader) throws IOException {
        this.synonyms = new ArrayList<>();
        try (InputStream stream = resourceLoader.openResource("synonyms.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
             String line;
             while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                List<String> list = Arrays.asList(line.split(","));
                this.synonyms.add(new HashSet<>(list));
                for (String l : list) {
                    l = l.toLowerCase();
                    if (l.contains(" ")) {
                        String key = l.split(" ")[0];
                        Set<String> set = mapSpace.getOrDefault(key, new HashSet<>());
                        set.add(l);
                        mapSpace.put(key, set);
                    }
                }
                if (list.size() == 2) {
                    String wordN = "";
                    String wordS = " ";
                    if (list.get(0).contains(" ") && !list.get(1).contains(" ")) {
                        wordN = list.get(0);
                        wordS = list.get(1);
                    } else if (list.get(1).contains(" ") && !list.get(0).contains(" ")) {
                        wordN = list.get(1);
                        wordS = list.get(0);
                    }
                    if (wordS.equals(wordN.replace(" ", ""))) {
                        mapBlank.put(wordS, wordN);
                    }
                }
            }
            synonymsMap = getSyMap(synonyms);
        }
    }


    /**
     * 同义词 对应 同义词集合
     * @param synonyms
     * @return
     */
    private Map<String,Set<String>> getSyMap(List<Set<String>> synonyms) {
        Map<String,Set<String>> map = new HashMap<>();
        for (Set<String> set: synonyms) {
            Set<String> allSet = new HashSet<>();
            Set<String> newSet = new HashSet<>();
            newSet.addAll(set);
            allSet.addAll(set);
            while (true){
                Set<String> addSet = new HashSet<>();
                for (String s: newSet){
                    Set<String> nSet = map.getOrDefault(s, new HashSet<>());
                    for (String ns: nSet){
                        if (!allSet.contains(ns)){
                            addSet.add(ns);
                        }
                    }
                }
                if (addSet.size() == 0){
                    break;
                }else {
                    allSet.addAll(addSet);
                    newSet = addSet;
                }
            }
            for (String s : allSet) {
                Set<String> nSet = map.getOrDefault(s, new HashSet<>());
                nSet.addAll(set);
                map.put(s, nSet);
            }
        }
        return map;
    }
}