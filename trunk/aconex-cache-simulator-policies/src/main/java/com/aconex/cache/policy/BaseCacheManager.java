package com.aconex.cache.policy;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCacheManager<T> implements CacheManager<T> {
    public List<CacheManagerListener<T>> listeners = new ArrayList<CacheManagerListener<T>>();
    
    public void addListener(CacheManagerListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(CacheManagerListener<T> listener) {
        listeners.remove(listener);
    }
    
    protected void evict(T object) {
        for (CacheManagerListener<T> listener : listeners) {
            listener.evictObject(object);
        }
    }

    protected void load(T object) {
        for (CacheManagerListener<T> listener : listeners) {
            listener.loadObject(object);
        }
    }

}
