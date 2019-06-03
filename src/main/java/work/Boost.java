package work;

import java.util.HashMap;
import java.util.Map;

public class Boost {
    private String productsId;
    private float orderAvg;
    private float orderUV;
    private float UV;

    public String getProductsId() {
        return productsId;
    }

    public float getOrderAvg() {
        return orderAvg;
    }

    public float getOrderUV() {
        return orderUV;
    }

    public float getUV() {
        return UV;
    }

    public void setProductsId(String productsId) {
        this.productsId = productsId;
    }

    public void setOrderAvg(float orderAvg) {
        this.orderAvg = orderAvg;
    }

    public void setOrderUV(float orderUV) {
        this.orderUV = orderUV;
    }

    public void setUV(float UV) {
        this.UV = UV;
    }

    public Boost(String productsId) {
        this.productsId = productsId;
        this.orderAvg = 0.0f;
        this.orderUV = 0.0f;
        this.UV = 0.0f;
    }


    public static Map<String, Boost> addBoost(String boostString){
        if (null == boostString){
            return null;
        }
        Map<String, Boost> map = new HashMap<>();
        boostString = boostString.replace("bgfunkey(products_id,order_score,ps_stock,", "");
        boostString = boostString.substring(1, boostString.length() - 2);
        String[] values = boostString.split("','");
        if (values.length == 3) {
            String[] orderArgValues = values[0].split(",");
            for (String orderArgValue: orderArgValues){
                String[] vs = orderArgValue.split(":");
                if (vs.length == 2) {
                    Boost boost = map.getOrDefault(vs[0], new Boost(vs[0]));
                    boost.setOrderAvg(Float.parseFloat(vs[1]));
                    map.put(vs[0], boost);
                }
            }

            String[] orderUValues = values[1].split(",");
            for (String orderUValue: orderUValues){
                String[] vs = orderUValue.split(":");
                if (vs.length == 2) {
                    Boost boost = map.getOrDefault(vs[0], new Boost(vs[0]));
                    boost.setOrderUV(Float.parseFloat(vs[1]));
                    map.put(vs[0], boost);
                }
            }

            String[] UVValues = values[2].split(",");
            for (String UVVValue: UVValues){
                String[] vs = UVVValue.split(":");
                if (vs.length == 2) {
                    Boost boost = map.getOrDefault(vs[0], new Boost(vs[0]));
                    boost.setUV(Float.parseFloat(vs[1]));
                    map.put(vs[0], boost);
                }
            }
        }
        return map;
    }
}
