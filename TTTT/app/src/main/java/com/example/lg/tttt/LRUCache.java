package com.example.lg.tttt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by LG on 2017-10-27.
 */

public class LRUCache<K, V>
{
    private static final float mHashTableLoadFactor = 0.75f;
    private LinkedHashMap<K, V> mMap;
    private int mCacheSize;

    public LRUCache(int cacheSize)
    {
        this.mCacheSize = cacheSize;
        int hashTableCapacity = (int) Math.ceil(cacheSize / mHashTableLoadFactor) + 1;
        mMap = new LinkedHashMap<K, V>(hashTableCapacity, mHashTableLoadFactor, true)
        {
            private static final long serialVersionUID = 1;

            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
            {
                return size() > LRUCache.this.mCacheSize;
            }
        };
    }

    public synchronized boolean containKey(String key)
    {
        return mMap.containsKey(key);
    }

    public synchronized V get(K key)
    {
        return mMap.get(key);
    }

    public synchronized void put(K key, V value)
    {
        mMap.put(key, value);
    }

    public synchronized int usedEntries()
    {
        return mMap.size();
    }

    public synchronized Collection<Map.Entry<K, V>> getAll()
    {
        return new ArrayList<Map.Entry<K, V>>(mMap.entrySet());
    }
}
