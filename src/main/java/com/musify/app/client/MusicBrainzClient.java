package com.musify.app.client;

import com.musify.app.dto.MusicBrainzResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MusicBrainzClient {

    private final String jsonFormat = "json";
    private final String urlRelsReleaseGroups =  "url-rels+release-groups";
    private final WebClient webClient;

    public MusicBrainzClient(WebClient.Builder builder,  @Value("${musicbrainz-url}") String musicBrainzUrl) {
        this.webClient = builder.baseUrl(musicBrainzUrl).build();
    }

    public Mono<MusicBrainzResponse> getArtist(String mbId) {
        return webClient
            .get()
            .uri(
                uriBuilder -> uriBuilder
                    .path("/artist/{mbId}")
                    .queryParam("fmt", "{format}")
                    .queryParam("inc", "{inc}")
                    .build(mbId, jsonFormat, urlRelsReleaseGroups)
            )
            .retrieve()
            .bodyToMono(MusicBrainzResponse.class);
    }


}
