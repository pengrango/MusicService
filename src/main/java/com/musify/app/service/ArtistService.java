package com.musify.app.service;

import com.musify.app.client.CoverArtArchiveClient;
import com.musify.app.client.MusicBrainzClient;
import com.musify.app.client.WikiDataClient;
import com.musify.app.client.WikiPediaClient;
import com.musify.app.dto.ArtistDetailResponse;
import com.musify.app.dto.CoverArtResponse;
import com.musify.app.dto.WikiPediaResponse;
import com.musify.app.util.ArtistInfoUtil;
import com.musify.app.dto.MusicBrainzResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ArtistService {

    private final MusicBrainzClient musicBrainzClient;
    private final WikiDataClient wikiDataClient;
    private final WikiPediaClient wikiPediaClient;
    private final CoverArtArchiveClient coverArtArchiveClient;
    private final String wikiDataUrl;
    Logger logger = LoggerFactory.getLogger(ArtistService.class);

    public ArtistService(
        MusicBrainzClient musicBrainzClient,
        WikiDataClient wikiDataClient,
        WikiPediaClient wikiPediaClient,
        CoverArtArchiveClient coverArtArchiveClient,
        @Value("$" + "{wikidata-url}") String wikiDataUrl
    ) {
        this.musicBrainzClient = musicBrainzClient;
        this.wikiDataClient = wikiDataClient;
        this.wikiPediaClient = wikiPediaClient;
        this.coverArtArchiveClient = coverArtArchiveClient;
        this.wikiDataUrl = wikiDataUrl;
    }

    public Mono<ArtistDetailResponse> getArtistDetail(String mbId) {
        return musicBrainzClient
            .getArtist(mbId)
            .flatMap(musicBrainzResponse -> {
                Flux<ArtistDetailResponse.Album> coverArtResponse =
                    fetchCoverArtInParallel(musicBrainzResponse.getReleaseGroups());
                Optional<String> itemOpt = ArtistInfoUtil.extractWikiDataItem(musicBrainzResponse);
                Mono<WikiPediaResponse> wikiPediaResponse = itemOpt
                    .map(this::fetchWikiDataAndPedia)
                    .orElseGet(() -> Mono.just(new WikiPediaResponse()));
                return Mono.zip(
                    coverArtResponse.collectList(),
                    wikiPediaResponse,
                    (coverArtList, wikiPediaResp) -> constructArtistDetail(coverArtList, wikiPediaResp, musicBrainzResponse, mbId));
            });
    }

    private Flux<ArtistDetailResponse.Album> fetchCoverArtInParallel(List<MusicBrainzResponse.ReleaseGroup> releaseGroups) {
        return Flux
            .fromIterable(releaseGroups)
            .parallel()
            .runOn(Schedulers.parallel())
            .flatMap(releaseGroup -> coverArtArchiveClient
                .getCoverArt(releaseGroup.getId())
                .onErrorResume(e -> {
                    logAccordingToException(e, "Failed to fetch data from Cover Art Archive");
                    return Mono.just(generateEmptyCoverArt());
                })
                .map(coverArtResponse -> getAlbums(releaseGroup, coverArtResponse))
            )
            .sequential()
            .flatMapIterable(Function.identity()); // list -> list
    }

    private CoverArtResponse generateEmptyCoverArt() {
        CoverArtResponse coverArtResponse = new CoverArtResponse();
        coverArtResponse.setImages(List.of());
        return coverArtResponse;
    }

    private List<ArtistDetailResponse.Album> getAlbums(MusicBrainzResponse.ReleaseGroup group,
                                                      CoverArtResponse response) {
        if (response.getImages().isEmpty()) {
            return List.of(new ArtistDetailResponse.Album(group.getId(), group.getTitle(), null));
        }
        return response
            .getImages()
            .stream()
            .filter(image -> Boolean.TRUE.equals(image.getFront()))
            .map(image -> new ArtistDetailResponse.Album(group.getId(), group.getTitle(), image.getImage()))
            .collect(Collectors.toList());
    }

    private Mono<WikiPediaResponse> fetchWikiDataAndPedia(String item) {
        return wikiDataClient
            .getEntityData(item)
            .map(resp -> ArtistInfoUtil.extractWikipediaTitle(resp, item))
            .flatMap(this.wikiPediaClient::getSummary)
            .onErrorResume(e -> {
                logAccordingToException(e, "Failed to fetch data from wikiData and wikiPedia");
                return Mono.just(new WikiPediaResponse());
            });
    }

    private void logAccordingToException(Throwable e, String message) {
        if (e instanceof WebClientResponseException
            &&
            ((WebClientResponseException) e).getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
            return;
        }
        logger.error(message, e);
    }

    private ArtistDetailResponse constructArtistDetail(
        List<ArtistDetailResponse.Album> albums,
        WikiPediaResponse wikiPediaResp,
        MusicBrainzResponse musicBrainzResp,
        String mbId
    ) {
        return new ArtistDetailResponse(
            mbId,
            musicBrainzResp.getName(),
            musicBrainzResp.getGender(),
            musicBrainzResp.getCountry(),
            musicBrainzResp.getDisambiguation(),
            wikiPediaResp.getExtractHtml(),
            albums
        );
    }
}
