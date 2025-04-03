package com.chanwoopark.service.unifiedbiztool.advertisement.meta.controller;


import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.ExcelResponse;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.service.MetaService;
import com.chanwoopark.service.unifiedbiztool.common.model.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/meta")
@RequiredArgsConstructor
@RestController
public class MetaController {

    private final MetaService metaService;

    @PostMapping("/upload")
    public ResponseEntity<Response<ExcelResponse>> uploadExcel(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                Response.of(
                        HttpStatus.OK,
                        metaService.processExcel(file)
                )
        );
    }
}
