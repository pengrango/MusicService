package com.musify.app.controller;

import com.musify.app.dto.ArtistDetailResponse;
import com.musify.app.service.ArtistService;
import com.musify.app.service.ArtistCacheService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/music-artist")
public class MusicArtistController {

    private ArtistService service;
    private ArtistCacheService artistCacheService;

    public MusicArtistController(ArtistService service, ArtistCacheService artistCacheService) {
        this.service = service;
        this.artistCacheService = artistCacheService;
    }

    @GetMapping("/details/{mbId}")
    public Mono<ArtistDetailResponse> getMusicArtist(@PathVariable String mbId) {
        Optional<ArtistDetailResponse> cachedArtistOpt = this.artistCacheService.getDataFromCache(mbId);
        return cachedArtistOpt
            .map(Mono::just)
            .orElseGet(() -> service
                .getArtistDetail(mbId)
                .flatMap(artistDetailResponse -> {
                    this.artistCacheService.putData(artistDetailResponse);
                    return Mono.just(artistDetailResponse);
                }));
    }

}
