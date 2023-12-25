package com.musify.app.client;

import com.musify.app.dto.CoverArtResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Component
public class CoverArtArchiveClient {

    private final WebClient webClient;

    public CoverArtArchiveClient(WebClient.Builder builder, @Value("${cover-art-archive-url}")  String coverArtArchiveUrl) {
        this.webClient = builder.baseUrl(coverArtArchiveUrl)
                                .clientConnector(new ReactorClientHttpConnector(
                                    HttpClient.create().followRedirect(true)
                                )).build();
    }

    public Mono<CoverArtResponse> getCoverArt(String groupId) {
        return webClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/release-group/{group}")
                .build(groupId))
            .retrieve()
            .bodyToMono(CoverArtResponse.class);
    }
}
