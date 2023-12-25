package com.musify.app.service;

import com.musify.app.dao.ArtistCache;
import com.musify.app.dto.ArtistDetailResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ArtistCacheService {
    ArtistCache artistCache;

    public ArtistCacheService(ArtistCache artistCache) {
        this.artistCache = artistCache;
    }

    public Optional<ArtistDetailResponse> getDataFromCache(String mbId) {
        return this.artistCache.getData(mbId);
    }

    public void putData(ArtistDetailResponse response) {
        this.artistCache.putData(response);
    }
}
