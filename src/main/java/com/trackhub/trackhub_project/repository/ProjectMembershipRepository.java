package com.trackhub.trackhub_project.repository;

import com.trackhub.trackhub_project.entity.ProjectMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMembershipRepository extends JpaRepository<ProjectMembership, Long> {

    Optional<ProjectMembership> findByUserIdAndProjectId(Long userId, Long projectId);

    List<ProjectMembership> findByProjectId(Long projectId);

    List<ProjectMembership> findByUserId(Long userId);
}