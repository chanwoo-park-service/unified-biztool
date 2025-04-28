package com.chanwoopark.service.unifiedbiztool.advertisement.meta.service;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.ThumbnailRequest;
import com.chanwoopark.service.unifiedbiztool.common.model.file.CustomMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class ThumbnailService {

    public ThumbnailRequest extractThumbnail(MultipartFile videoFile) throws IOException {
        File tempVideoFile = File.createTempFile("video_", ".tmp");
        File thumbnailFile = File.createTempFile("thumbnail_", ".jpg");

        try {
            videoFile.transferTo(tempVideoFile);

            List<String> command = List.of(
                    "ffmpeg",
                    "-i", tempVideoFile.getAbsolutePath(),
                    "-vframes", "1",  // 첫 번째 프레임만
                    "-an",  // 오디오 제외
                    "-y",   // 기존 파일 덮어쓰기
                    thumbnailFile.getAbsolutePath()
            );

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            // 프로세스 결과 처리
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    StringBuilder errorOutput = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                    throw new IOException("FFmpeg 처리 실패: " + errorOutput);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("처리가 중단되었습니다", e);
            }

            return ThumbnailRequest.builder()
                    .absoluteFile(thumbnailFile)
                    .multipartFile(
                            new CustomMultipartFile(
                                    thumbnailFile,
                                    "thumbnail",
                                    videoFile.getOriginalFilename() + ".jpg",
                                    "image/jpeg"
                            )
                    )
                    .build();

        } finally {
            // 임시 비디오 파일 삭제 (썸네일은 반환하므로 삭제하지 않음)
            if (tempVideoFile.exists()) {
                tempVideoFile.delete();
            }
        }
    }
}
