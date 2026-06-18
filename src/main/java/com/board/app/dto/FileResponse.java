package com.board.app.dto;

import com.board.app.entity.FileAttachment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileResponse {
    private Long id;
    private String originalName;
    private Long fileSize;
    private String contentType;

    public static FileResponse from(FileAttachment file) {
        return FileResponse.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .fileSize(file.getFileSize())
                .contentType(file.getContentType())
                .build();
    }
}