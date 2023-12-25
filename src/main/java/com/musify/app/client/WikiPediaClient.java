package com.musify.app.client;

import com.musify.app.dto.WikiPediaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class WikiPediaClient {
    private final WebClient webClient;

    public WikiPediaClient(WebClient.Builder builder, @Value("${wikipedia-url}") String wikiPediaUrl) {
        this.webClient = builder.baseUrl(wikiPediaUrl).build();
    }

    public Mono<WikiPediaResponse> getSummary(String title) {
        return webClient
            .get()
            .uri(
                uriBuilder -> uriBuilder
                    .path("/page/summary/{title}")
                    .build(title)
            )
            .retrieve()
            .bodyToMono(WikiPediaResponse.class);
    }
}
