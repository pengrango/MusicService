package com.musify.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musify.app.client.CoverArtArchiveClient;
import com.musify.app.client.MusicBrainzClient;
import com.musify.app.client.WikiDataClient;
import com.musify.app.client.WikiPediaClient;
import com.musify.app.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArtistServiceTest {

    private final String wikiDataUrl = "https://www.wikidata.org/wiki";
    private final String title = "Got to Be There";
    private final String id = "97e0014d-a267-33a0-a868-bb4e2552918a";
    private final String image = "http://coverartarchive.org/release/6402fbc9-cb27-4306-96eb-10e6dc489aaf/12592766269" +
        ".jpg";
    private final String michaelJackson = "Michael_Jackson";
    private final String mbId = "testMbid";
    private final String q2831 = "Q2831";
    private MusicBrainzClient musicBrainzClient;
    private WikiDataClient wikiDataClient;
    private WikiPediaClient wikiPediaClient;
    private CoverArtArchiveClient coverArtArchiveClient;
    private ArtistService artistService;
    private MusicBrainzResponse mockedMusicBrainz;
    private WikiDataResponse mockedWikiDataResp;
    private WikiPediaResponse mockedWPediaResp;
    private CoverArtResponse mockCoverArtResp;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        musicBrainzClient = mock(MusicBrainzClient.class);
        wikiDataClient = mock(WikiDataClient.class);
        wikiPediaClient = mock(WikiPediaClient.class);
        coverArtArchiveClient = mock(CoverArtArchiveClient.class);
        artistService = new ArtistService(musicBrainzClient, wikiDataClient, wikiPediaClient,
            coverArtArchiveClient, wikiDataUrl);
        ObjectMapper objectMapper = new ObjectMapper();
        File mockMusicBrainzResp = new File("src/test/resources/MockMusicBrainzResp.json"); // Replace with your JSON
        mockedMusicBrainz = objectMapper.readValue(mockMusicBrainzResp, MusicBrainzResponse.class);

        File mockWikiData = new File("src/test/resources/MockWikiDataResp.json");
        mockedWikiDataResp = objectMapper.readValue(mockWikiData, WikiDataResponse.class);

        File mockWPediaData = new File("src/test/resources/MockWikiPediaResp.json");
        mockedWPediaResp = objectMapper.readValue(mockWPediaData, WikiPediaResponse.class);

        File mockArtResp1 = new File("src/test/resources/MockCoverArtResp.json");
        mockCoverArtResp = objectMapper.readValue(mockArtResp1, CoverArtResponse.class);

    }

    @Test
    public void shouldReturnAllDataWhenAllExternalServicesOK() throws IOException {
        when(musicBrainzClient.getArtist(mbId))
            .thenReturn(Mono.just(mockedMusicBrainz));
        when(wikiDataClient.getEntityData(q2831))
            .thenReturn(Mono.just(mockedWikiDataResp));
        when(wikiPediaClient.getSummary(michaelJackson))
            .thenReturn(Mono.just(mockedWPediaResp));
        when(coverArtArchiveClient.getCoverArt(any()))
            .thenReturn(Mono.just(mockCoverArtResp));

        Mono<ArtistDetailResponse> result = artistService.getArtistDetail(mbId);

        StepVerifier.create(result).expectNextMatches(resp -> {
            checkOtherFields(resp);

            resp.getAlbums().forEach(album -> {
                assertEquals(id, album.getId());
                assertEquals(title, album.getTitle());
                assertEquals(image, album.getImageUrl());
            });

            return true;
        }).verifyComplete();
    }

    @Test
    public void shouldExcludeImageUrlWhenCoverArtReturnNotFound() {
        // Given
        when(musicBrainzClient.getArtist(mbId))
            .thenReturn(Mono.just(mockedMusicBrainz));
        when(wikiDataClient.getEntityData(q2831))
            .thenReturn(Mono.just(mockedWikiDataResp));
        when(wikiPediaClient.getSummary(michaelJackson))
            .thenReturn(Mono.just(mockedWPediaResp));
        when(coverArtArchiveClient.getCoverArt(any()))
            .thenReturn(
                Mono.error(WebClientResponseException.create(
                    404,
                    "Could not found cover",
                    HttpHeaders.EMPTY,
                    null,
                    null
                    )
                )
            );

        // When
        Mono<ArtistDetailResponse> result = artistService.getArtistDetail(mbId);
        //Then
        StepVerifier.create(result).expectNextMatches(resp -> {
            checkOtherFields(resp);

            resp.getAlbums().forEach(album -> {
                assertEquals(id, album.getId());
                assertEquals(title, album.getTitle());
                assertNull(album.getImageUrl());
            });

            return true;
        }).verifyComplete();
    }

    @Test
    public void shouldReturnBadRequestWhenMusicBrainzFail() {
        when(musicBrainzClient.getArtist(mbId))
            .thenReturn(
            Mono.error(WebClientResponseException.create(
                    400,
                    "Bad Request",
                    HttpHeaders.EMPTY,
                    null,
                    null
                )
            )
        );;

        Mono result = artistService.getArtistDetail(mbId);

        StepVerifier.create(result).expectErrorMatches(throwable -> {
            assertTrue(throwable instanceof WebClientResponseException.BadRequest);
            return true;
        }).verify();
    }

    private void checkOtherFields(ArtistDetailResponse resp) {
        assertEquals("Michael Jackson", resp.getName());
        assertEquals("US", resp.getCountry());
        assertEquals(mbId, resp.getMbId());
        assertEquals("Male", resp.getGender());
        assertEquals("“King of Pop”", resp.getDisambiguation());
        assertEquals(mockedWPediaResp.getExtractHtml(), resp.getDescription());
    }
}
