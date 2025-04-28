package com.chanwoopark.service.unifiedbiztool.advertisement.meta.controller;


import com.chanwoopark.service.unifiedbiztool.advertisement.meta.exception.HttpClientException;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.MetaAccountResponse;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.web.AdRequest;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.web.AdResponse;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.web.ExcelResponse;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.service.MetaService;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.validation.MetaValidator;
import com.chanwoopark.service.unifiedbiztool.common.exception.TokenNotFoundException;
import com.chanwoopark.service.unifiedbiztool.common.model.dto.Response;
import com.chanwoopark.service.unifiedbiztool.common.model.dto.TokenRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequestMapping("/api/meta")
@RequiredArgsConstructor
@RestController
public class MetaController {

    private final MetaService metaService;

    private final MetaValidator metaValidator;

    @GetMapping("/accounts")
    public ResponseEntity<Response<List<MetaAccountResponse>>> accounts() {
        return ResponseEntity.ok(Response.of(
                HttpStatus.OK,
                metaService.getAccounts()
        ));
    }

    @PostMapping("/upload")
    public ResponseEntity<Response<List<ExcelResponse>>> uploadExcel(@RequestParam("file") MultipartFile file) throws IOException {
        metaService.validateExcel(file);
        return ResponseEntity.ok(
                Response.of(
                        HttpStatus.OK,
                        metaService.processExcel(file)
                )
        );
    }

    @PostMapping("/publish")
    public ResponseEntity<Response<AdResponse>> publishAd(
            @RequestPart("request") @Valid AdRequest adRequest,
            @RequestPart(name = "files", required = false) List<MultipartFile> files
    ) throws JsonProcessingException {
        metaValidator.validateCreativeFormat(
                adRequest,
                files
        );
        return ResponseEntity.ok(
                Response.of(
                        HttpStatus.OK,
                        metaService.publishAd(adRequest, files)
                )
        );
    }

    @GetMapping("/token")
    public ResponseEntity<String> checkTokenHealth() {
        try {
            metaService.validateToken();
            return ResponseEntity.ok().build();
        } catch (TokenNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (HttpClientException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/token")
    public ResponseEntity<Void> insertToken(@RequestBody TokenRequest tokenRequest) {
        metaService.insertToken(tokenRequest.accessToken());
        return ResponseEntity.ok().build();
    }
}
