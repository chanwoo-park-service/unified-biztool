package com.chanwoopark.service.unifiedbiztool.common.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component
public class HttpClientHelper {

    private final WebClient webClient;

    public String get(String url) {
        log.info("GET {}", url);
        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.warn("GET 실패 - 상태: {}, 본문: {}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("GET 실패 (" + url + "): " + errorBody.replace("\"", "")));
                                })
                )
                .bodyToMono(String.class)
                .block();
    }

    public String postForm(String url, Consumer<BodyInserters.FormInserter<String>> formBuilder) {
        BodyInserters.FormInserter<String> formData = BodyInserters.fromFormData("", "");
        formBuilder.accept(formData);
        log.info("POST {}", url);
        return webClient.post()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(formData)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.warn("POST 실패 - 상태: {}, 본문: {}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("POST 실패 (" + url + "): " + errorBody.replace("\"", "")));
                                })
                )
                .bodyToMono(String.class)
                .block();
    }

}
