package com.chanwoopark.service.unifiedbiztool.common.http;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.exception.HttpClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Map;
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
                                    return Mono.error(new HttpClientException(clientResponse.statusCode().value(), "GET : " + errorBody));
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
                                    return Mono.error(new HttpClientException(clientResponse.statusCode().value(), "POST : " + errorBody));
                                })
                )
                .bodyToMono(String.class)
                .block();
    }

    public String postFormIgnoreFail(String url, Consumer<BodyInserters.FormInserter<String>> formBuilder) {
        BodyInserters.FormInserter<String> formData = BodyInserters.fromFormData("", "");
        formBuilder.accept(formData);
        log.info("POST {}", url);
        return webClient.post()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(formData)
                .exchangeToMono(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .doOnNext(body -> log.warn("POST 실패 - 상태: {}, 본문: {}", response.statusCode(), body));
                    }
                    return response.bodyToMono(String.class);
                })
                .block();
    }

    public String postMultipart(String url, Consumer<MultipartBodyBuilder> builderConsumer) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builderConsumer.accept(builder);
        log.info("POST Multipart {}", url);
        return webClient.post()
                .uri(url)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.warn("POST 실패 - 상태: {}, 본문: {}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new HttpClientException(clientResponse.statusCode().value(), "POST : " + errorBody));
                                })
                )
                .bodyToMono(String.class)
                .block();
    }

    public String postMultipartIgnoreFail(String url, Consumer<MultipartBodyBuilder> builderConsumer) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builderConsumer.accept(builder);
        log.info("POST Multipart {}", url);
        return webClient.post()
                .uri(url)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchangeToMono(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .doOnNext(body -> log.warn("POST 실패 - 상태: {}, 본문: {}", response.statusCode(), body));
                    }
                    return response.bodyToMono(String.class);
                })
                .block();
    }

    public String delete(String url, Map<String, String> params) {
        log.info("DELETE {}", url);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        params.forEach(builder::queryParam);

        return webClient.delete()
                .uri(builder.build().toUriString())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.warn("DELETE 실패 - 상태: {}, 본문: {}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new HttpClientException(clientResponse.statusCode().value(), "DELETE : " + errorBody));
                                })
                )
                .bodyToMono(String.class)
                .block();
    }

}
