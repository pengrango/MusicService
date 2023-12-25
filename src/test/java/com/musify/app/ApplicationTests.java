package com.musify.app;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(locations = "/application-test.properties") // Override external-url
public class ApplicationTests {

    private static final String MB = "mb";
    private static final String WIKIPEDIA = "wikipedia";
    private static final String WIKIDATA = "wikidata";
    private static final String COVER = "cover";
    private static MockWebServer mockWebServer;
    private String basePath = "/music-artist/details/";
    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();

        mockWebServer.start(8080);
    }

    private static String getFirstPath(RecordedRequest request) {
        return Path.of(request.getPath()).getName(0).toString();
    }

    private static String getMockResponse(String path) {
        Path filePath = Path.of(path);
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void shouldReturnDataWhenExternalServicesWorks() {
        setMockWebServerBehaviour("", 0);
        String mbId = UUID.randomUUID().toString();

        getArtistDetailsAndAssertCommonQuestions(mbId)
            .jsonPath("$.description").exists()
            .jsonPath("$.albums[0].imageUrl").exists();
    }

    @Test
    public void shouldReturnDataExceptDescriptionWhenWikiDataFails() {
        setMockWebServerBehaviour(WIKIDATA, HttpStatus.NOT_FOUND.value());
        String mbId = UUID.randomUUID().toString();

        getArtistDetailsAndAssertCommonQuestions(mbId)
            .jsonPath("$.description").doesNotExist()
            .jsonPath("$.albums[0].imageUrl").exists();
    }

    @Test
    public void shouldReturnDataExceptDescriptionWhenWikiPediaFails() {
        setMockWebServerBehaviour(WIKIPEDIA, HttpStatus.NOT_FOUND.value());
        String mbId = UUID.randomUUID().toString();

        getArtistDetailsAndAssertCommonQuestions(mbId)
            .jsonPath("$.description").doesNotExist()
            .jsonPath("$.albums[0].imageUrl").exists();
    }

    @Test
    public void shouldReturnDataExceptImageWhenCoverArtFails() {
        setMockWebServerBehaviour(COVER, HttpStatus.NOT_FOUND.value());
        String mbId = UUID.randomUUID().toString();

        getArtistDetailsAndAssertCommonQuestions(mbId)
            .jsonPath("$.description").exists()
            .jsonPath("$.albums[0].imageUrl").doesNotExist();
    }

    @Test
    public void shouldReturn400WhenMusicBrainzFails() {
        setMockWebServerBehaviour(MB, HttpStatus.BAD_REQUEST.value());

        getArtistDetailsAndGetFailedResponse();
    }

    @Test
    public void shouldNotQueryExternalServiceWhenCached() {
        setMockWebServerBehaviour("", 0);
        String mbId = UUID.randomUUID().toString();

        getArtistDetailsAndAssertCommonQuestions(mbId)
            .jsonPath("$.description").exists()
            .jsonPath("$.albums[0].imageUrl").exists();

        setMockWebServerBehaviour(MB, HttpStatus.BAD_REQUEST.value()); //will not be called due to cached

        getArtistDetailsAndAssertCommonQuestions(mbId)
            .jsonPath("$.description").exists()
            .jsonPath("$.albums[0].imageUrl").exists();
    }

    private void setMockWebServerBehaviour(String serviceToFail, int statusCodeIfFail) {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                switch (getFirstPath(request)) {
                    case MB:
                        return generateMockResponse("src/test/resources/MockMusicBrainzResp.json",
                            MB.equals(serviceToFail), statusCodeIfFail);
                    case WIKIDATA:
                        return generateMockResponse("src/test/resources/MockWikiDataResp.json",
                            WIKIDATA.equals(serviceToFail), statusCodeIfFail);
                    case WIKIPEDIA:
                        return generateMockResponse("src/test/resources/MockWikiPediaResp.json",
                            WIKIPEDIA.equals(serviceToFail), statusCodeIfFail);
                    case COVER:
                        return generateMockResponse("src/test/resources/MockCoverArtResp.json",
                            COVER.equals(serviceToFail), statusCodeIfFail);
                    default:
                        return new MockResponse().setResponseCode(404);
                }
            }
        });
    }

    private MockResponse generateMockResponse(String mockDataFile, boolean mockFailure, int statusCodeIfFail) {
        return mockFailure ?
            new MockResponse()
                .setResponseCode(statusCodeIfFail)
                .setBody("Mock a failed request")
                .addHeader("Content-Type", "application/json")
            :
            new MockResponse()
                .setResponseCode(200)
                .setBody(getMockResponse(mockDataFile))
                .addHeader("Content-Type", "application/json");
    }

    private WebTestClient.ResponseSpec getArtistDetailsAndGetFailedResponse() {
        String mbId = UUID.randomUUID().toString();
        String path = Path.of(basePath).resolve(mbId).toString();
        return webTestClient
            .get()
            .uri(path)
            .exchange()
            .expectStatus().isBadRequest();
    }

    private WebTestClient.BodyContentSpec getArtistDetailsAndAssertCommonQuestions(String mbId) {
        String path = Path.of(basePath).resolve(mbId).toString();

        return webTestClient
            .get()
            .uri(path)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo("Michael Jackson")
            .jsonPath("$.country").isEqualTo("US")
            .jsonPath("$.disambiguation").isEqualTo("“King of Pop”")
            .jsonPath("$.albums[0].id").isEqualTo("97e0014d-a267-33a0-a868-bb4e2552918a")
            .jsonPath("$.albums[0].title").isEqualTo("Got to Be There")
            .jsonPath("$.mbid").isEqualTo(mbId);
    }
}