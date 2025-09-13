package com.trackhub.trackhub_project.repository;

import com.trackhub.trackhub_project.entity.AudioFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AudioFileRepository extends JpaRepository<AudioFile, Long> {

    List<AudioFile> findByProjectId(Long projectId);

    Optional<AudioFile> findByIdAndProjectId(Long id, Long projectId);
}