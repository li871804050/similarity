package work;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 词距处理相关
 */
public class Model {

    /**
     * 使用注意非空判断
     * 获取型号组合
     * @param queryList
     * @return
     */
    public Set<List<String>> getModels(List<String> queryList) {
        Set<List<String>> modelSet = new HashSet<>();
        for (int i = 0; i < queryList.size(); i++) {
            if (queryList.get(i).matches("[0-9]+")) {
                modelSet.add(getModelQuery(i, queryList));
            }
        }
        return modelSet;
    }


    /**
     * 计算型号距离
     * @param model
     * @param words
     * @return
     */
    public float getModelDistance(List<String> model, List<String> words) {
        float score = 0.0f;
        for (int i = 0; i < model.size() - 1; i++) {
            int dis = getDistanceWords(model.get(i + 1), model.get(i), words);
            if (dis > 0) {
                score += 1.0f / (dis + 1.0f);
            }
        }
        return score;
    }

    /**
     * 计算同义词短语距离
     * @param model
     * @param words
     * @return
     */
    public float getModelDistance2(List<String> model, List<String> words) {
        float score = 0.0f;
        for (int i = 0; i < model.size() - 1; i++) {
            score += getDistanceScore(model.get(i + 1), model.get(i), words);
        }
        return score;
    }

    /**
     * 计算两词之间的最短距离
     * @param end
     * @param start
     * @param words
     * @return
     */
    public int getDistanceWords(String end, String start, List<String> words) {
        List<Integer> startList = findAll(words, start);
        List<Integer> endList = findAll(words, end);
        if (startList.size() > 0 && endList.size() > 0){
            int min = words.size() + 1;
            for (int e = 0; e <endList.size(); e++){
                for (int s = 0; s < startList.size(); s++){
                    int dis = endList.get(e) - startList.get(s);
                    dis = dis < 0? 0 - 2*dis:dis;
                    if (min > dis){
                        min = dis;
                    }
                }
            }
            return min;
        }else if (startList.size() > 0 || endList.size() > 0){
            return -2;
        }else {
            return -1;
        }
    }


    /**
     * AI计算两词的距离得分
     * xiao mi = 1.0
     * mi xiao = 0.1
     * xiao ** mi = 0.1
     * mi ** xiao = 0.1
     * mi == xiao = 0 不召回
     * 不存在0.0
     * @param end
     * @param start
     * @param words
     * @return
     */
    public float getDistanceScore(String end, String start, List<String> words) {
        List<Integer> startList = findAll(words, start);
        List<Integer> endList = findAll(words, end);
        if (startList.size() > 0 && endList.size() > 0){
            float max = 0.0f;
            for (int e = 0; e <endList.size(); e++){
                for (int s = 0; s < startList.size(); s++){
                    float score = 0.0f;
                    int dis = endList.get(e) - startList.get(s);
                   if (dis == 1){
                       score = 1.0f;
                   }else if (dis > 1 || dis <= -1){
                       score = 0.1f;
                   }

                    if (max < score){
                        max = score;
                    }
                }
            }
            return max;
        }else if (startList.size() > 0 || endList.size() > 0){
            return 0.0f;
        }else {
            return 0.0f;
        }
    }

    /**
     * 获取word在words中的所有位置
     * @param words
     * @param word
     * @return
     */
    private List<Integer> findAll(List<String> words, String word) {
        List<String> newWords = new ArrayList<>(words);
        List<Integer> list = new ArrayList<>();
        while (newWords.contains(word)){
            int index = newWords.indexOf(word);
            newWords.set(index, null);
            list.add(index);
        }
        return list;
    }

    /**
     * 获取以i为中心的型号数据
     *
     * @param i
     * @param queryList
     * @return
     */
    private List<String> getModelQuery(int i, List<String> queryList) {
        List<String> res = new ArrayList<>();
        res.add(queryList.get(i));
        /*向后查找满足条件的*/
        for (int j = i; j >= 1; j--) {
            String start = queryList.get(j);
            String end = queryList.get(j - 1);
            if (isModel(start) && isModel(end)) {
                res.add(0, end);
            } else {
                break;
            }
        }
        /*向前查找满足的*/
        for (int j = i; j < queryList.size() - 1; j++) {
            String start = queryList.get(j + 1);
            String end = queryList.get(j);
            if (isModel(start) && isModel(end)) {
                res.add(start);
            } else {
                break;
            }
        }
        return res;
    }

    /**
     * 判断是否满足型号要求
     * @param q
     * @return
     */
    private boolean isModel(String q) {
        if (q.matches("[0-9]+")) {
            return true;
        }
        if (q.matches("[a-z]+") && q.length() <= 3) {
            return true;
        }
        return false;
    }


    public float getModelDistances(Set<List<String>> modelSet, List<String> wList) {
        if (null == modelSet || modelSet.size() == 0 || null == wList || wList.size() == 0){
            return 0.0f;
        }
        float score = 0.0f;
        for (List<String> model: modelSet){
            score += getModelDistance(model, wList);
        }
        return score;
    }

    /**
     * 相邻距离计算
     *
     * @param num
     * @param en
     * @param words
     * @param l
     * @return
     */
    private float getDistance(String num, String en, List<String> words, int l) {
        if (en.length() > 3 && en.matches("[a-z]+")) {
            return 0;
        }
        if (words.contains(num) && words.contains(en)) {
            int dis = words.indexOf(num) - words.indexOf(en);
            dis = l * dis;
            dis = dis > 0 ? 2 * dis : 0 - dis;
            return 1.0f / (dis + 1.0f);
        } else {
            return 0;
        }
    }
}
