package app;

import java.util.HashMap;
import java.util.Map;

public class Cache {
    private Map<String, String> m1_cache;
    private Map<String, String> m2_cache;
    private Map<String, String> m3_cache;

    public Map<String, String> getM1_cache() {
        return m1_cache;
    }

    public Map<String, String> getM2_cache() {
        return m2_cache;
    }

    public Map<String, String> getM3_cache() {
        return m3_cache;
    }

    public Cache(){
        m1_cache = new HashMap<>();
        m2_cache = new HashMap<>();
        m3_cache = new HashMap<>();
    }

}
