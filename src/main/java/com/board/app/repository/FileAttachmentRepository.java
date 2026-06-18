package com.board.app.repository;

import com.board.app.entity.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {
    List<FileAttachment> findByPostId(Long postId);
    void deleteByPostId(Long postId);
}