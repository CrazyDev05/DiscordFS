/*
 * Iris is a World Generator for Minecraft Bukkit Servers
 * Copyright (c) 2022 Arcane Arts (Volmit Software)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.crazydev22.utils;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.EqualsAndHashCode;

import java.time.Duration;

@EqualsAndHashCode
public class Cache<K, V> {
    private final long max;
    private final LoadingCache<K, V> cache;
    private final CacheLoader<K, V> loader;

    public Cache(CacheLoader<K, V> loader, long max, Duration expiration) {
        this.max = max;
        this.loader = loader;
        this.cache = create(loader, expiration);
    }

    private LoadingCache<K, V> create(CacheLoader<K, V> loader, Duration expiration) {
        return Caffeine
                .newBuilder()
                .maximumSize(max)
                .initialCapacity((int) (max))
                .expireAfterWrite(expiration)
                .build((k) -> loader == null ? null : loader.load(k));
    }

    public void invalidate(K k) {
        cache.invalidate(k);
    }

    public void invalidate() {
        cache.invalidateAll();
    }

    public V get(K k) {
        return cache.get(k);
    }

    public long getSize() {
        return cache.estimatedSize();
    }

    public long getMaxSize() {
        return max;
    }

    public boolean contains(K next) {
        return cache.getIfPresent(next) != null;
    }
}
