package com.musify.app.client;

import com.musify.app.dto.WikiDataResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class WikiDataClient {
    private final WebClient webClient;

    public WikiDataClient(WebClient.Builder builder, @Value("${wikidata-url}") String wikiDataUrl) {
        this.webClient = builder
            .codecs(clientCodecConfigurer ->
                clientCodecConfigurer.defaultCodecs().maxInMemorySize(4 * 1024 * 1024)) // 4 MB
            .baseUrl(wikiDataUrl).build();
    }


    public Mono<WikiDataResponse> getEntityData(String item) {
        return webClient
            .get()
            .uri(uriBuilder -> uriBuilder.path("/Special:EntityData/{item}").build(item + ".json"))
            .retrieve()
            .bodyToMono(WikiDataResponse.class);
    }
}
