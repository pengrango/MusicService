package com.musify.app.dao;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.replicatedmap.ReplicatedMap;
import com.musify.app.dto.ArtistDetailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Scope("singleton")
public class ArtistCache {
    private final String mapName = "map";
    private final ReplicatedMap<String, ArtistDetailResponse> cacheMap;
    private HazelcastInstance hazelcastInstance;
    private int ttl;

    public ArtistCache(HazelcastInstance hazelcastInstance, @Value("$" + "{cache-time-to-live-seconds}") int ttl) {
        this.hazelcastInstance = hazelcastInstance;
        this.cacheMap = hazelcastInstance.getReplicatedMap(mapName);
        this.ttl = ttl;
    }

    public Optional<ArtistDetailResponse> getData(String mbId) {
        ArtistDetailResponse artistDetailResponse = cacheMap.get(mbId);
        return Optional.ofNullable(artistDetailResponse);
    }

    public void putData(ArtistDetailResponse response) {
        cacheMap.put(response.getMbId(), response, ttl, TimeUnit.SECONDS);
    }
}
