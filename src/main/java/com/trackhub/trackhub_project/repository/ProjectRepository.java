package com.trackhub.trackhub_project.repository;

import com.trackhub.trackhub_project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByCriador_Email(String email);

    boolean existsByIdAndCriador_Email(Long id, String email);
}