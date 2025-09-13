package com.trackhub.trackhub_project.controller;

import com.trackhub.trackhub_project.dto.AddMemberRequest;
import com.trackhub.trackhub_project.dto.ProjectRequest;
import com.trackhub.trackhub_project.dto.ProjectResponse;
import com.trackhub.trackhub_project.dto.UserResponse;
import com.trackhub.trackhub_project.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.trackhub.trackhub_project.dto.AudioFileResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProjectResponse>> getMyProjects() {
        List<ProjectResponse> projects = projectService.getMyProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        ProjectResponse project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id, @RequestBody ProjectRequest request) {
        ProjectResponse updatedProject = projectService.updateProject(id, request);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<ProjectResponse> addMember(@PathVariable Long projectId, @RequestBody AddMemberRequest request) {
        ProjectResponse project = projectService.addMember(projectId, request.email());
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{projectId}/members")
    public ResponseEntity<Void> removeMember(@PathVariable Long projectId, @RequestBody AddMemberRequest request) {
        projectService.removeMember(projectId, request.email());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<UserResponse>> getProjectMembers(@PathVariable Long projectId) {
        List<UserResponse> members = projectService.getProjectMembers(projectId);
        return ResponseEntity.ok(members);
    }

    @PostMapping("/{projectId}/upload")
    public ResponseEntity<String> uploadAudioFile(@PathVariable Long projectId, @RequestParam("file") MultipartFile file) {
        projectService.uploadAudioFile(projectId, file);
        return ResponseEntity.ok("File uploaded successfully.");
    }

    @GetMapping("/{projectId}/audio")
    public ResponseEntity<List<AudioFileResponse>> getProjectAudioFiles(@PathVariable Long projectId) {
        List<AudioFileResponse> audioFiles = projectService.getProjectAudioFiles(projectId);
        return ResponseEntity.ok(audioFiles);
    }

    @GetMapping("/{projectId}/audio/{audioId}/download")
    public ResponseEntity<Resource> downloadAudioFile(@PathVariable Long projectId, @PathVariable Long audioId) {
        Resource resource = projectService.downloadAudioFile(projectId, audioId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{projectId}/audio/{audioId}")
    public ResponseEntity<String> deleteAudioFile(@PathVariable Long projectId, @PathVariable Long audioId) {
        projectService.deleteAudioFile(projectId, audioId);
        return ResponseEntity.ok("Audio file deleted successfully.");
    }

    @DeleteMapping("/{projectId}/members/me")
    public ResponseEntity<String> leaveProject(@PathVariable Long projectId) {
        projectService.leaveProject(projectId);
        return ResponseEntity.ok("Successfully left the project.");
    }

    @GetMapping("/me")
    public ResponseEntity<List<ProjectResponse>> getAllMyProjects() {
        List<ProjectResponse> projects = projectService.getAllMyProjects();
        return ResponseEntity.ok(projects);
    }
}