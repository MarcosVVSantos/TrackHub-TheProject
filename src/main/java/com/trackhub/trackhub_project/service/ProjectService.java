package com.trackhub.trackhub_project.service;

import com.trackhub.trackhub_project.dto.AudioFileResponse;
import com.trackhub.trackhub_project.dto.ProjectRequest;
import com.trackhub.trackhub_project.dto.ProjectResponse;
import com.trackhub.trackhub_project.dto.UserResponse;
import com.trackhub.trackhub_project.entity.AudioFile;
import com.trackhub.trackhub_project.entity.Project;
import com.trackhub.trackhub_project.entity.ProjectMembership;
import com.trackhub.trackhub_project.entity.User;
import com.trackhub.trackhub_project.repository.AudioFileRepository;
import com.trackhub.trackhub_project.repository.ProjectMembershipRepository;
import com.trackhub.trackhub_project.repository.ProjectRepository;
import com.trackhub.trackhub_project.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AudioFileRepository audioFileRepository;

    private static final String BASE_UPLOAD_DIR = "src/main/resources/uploads/";
    private static final String AUDIO_DIR_PREFIX = "audio/";

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, ProjectMembershipRepository projectMembershipRepository, AudioFileRepository audioFileRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMembershipRepository = projectMembershipRepository;
        this.audioFileRepository = audioFileRepository;
    }

    // --- Métodos de Gerenciamento de Projetos ---
    public ProjectResponse createProject(ProjectRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User criador = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        var project = Project.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .status(request.status())
                .criador(criador)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
        projectRepository.save(project);
        return mapToProjectResponse(project);
    }

    public List<ProjectResponse> getMyProjects() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        List<Project> projects = projectRepository.findByCriador_Email(userEmail);
        return projects.stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found!"));
        if (!project.getCriador().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access Denied: You are not the creator of this project.");
        }
        return mapToProjectResponse(project);
    }

    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found!"));
        if (!project.getCriador().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access Denied: You are not the creator of this project.");
        }
        project.setNome(request.nome());
        project.setDescricao(request.descricao());
        project.setStatus(request.status());
        project.setDataAtualizacao(LocalDateTime.now());
        projectRepository.save(project);
        return mapToProjectResponse(project);
    }

    public void deleteProject(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found!"));
        if (!project.getCriador().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access Denied: You are not the creator of this project.");
        }
        projectRepository.deleteById(id);
    }

    // --- Métodos de Gerenciamento de Membros ---
    public ProjectResponse addMember(Long projectId, String memberEmail) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found!"));
        if (!project.getCriador().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access Denied: You are not the creator of this project.");
        }
        User member = userRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new RuntimeException("Member not found!"));
        if (projectMembershipRepository.findByUserIdAndProjectId(member.getId(), projectId).isPresent()) {
            throw new RuntimeException("Member already exists in this project.");
        }
        var membership = ProjectMembership.builder()
                .project(project)
                .user(member)
                .build();
        projectMembershipRepository.save(membership);
        return mapToProjectResponse(project);
    }

    public void removeMember(Long projectId, String memberEmail) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found!"));
        if (!project.getCriador().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access Denied: You are not the creator of this project.");
        }
        User memberToRemove = userRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new RuntimeException("Member not found!"));
        ProjectMembership membership = projectMembershipRepository.findByUserIdAndProjectId(memberToRemove.getId(), projectId)
                .orElseThrow(() -> new RuntimeException("Membership not found!"));
        projectMembershipRepository.delete(membership);
    }

    public void leaveProject(Long projectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found!"));
        if (project.getCriador().getEmail().equals(userEmail)) {
            throw new RuntimeException("Project creator cannot leave the project.");
        }
        ProjectMembership membership = projectMembershipRepository.findByUserIdAndProjectId(user.getId(), projectId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this project."));
        projectMembershipRepository.delete(membership);
    }

    public List<UserResponse> getProjectMembers(Long projectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        boolean isCreator = projectRepository.existsByIdAndCriador_Email(projectId, userEmail);
        boolean isMember = projectMembershipRepository.findByUserIdAndProjectId(userRepository.findByEmail(userEmail).get().getId(), projectId).isPresent();
        if (!isCreator && !isMember) {
            throw new RuntimeException("Access Denied: You are not a member or the creator of this project.");
        }
        List<ProjectMembership> memberships = projectMembershipRepository.findByProjectId(projectId);
        return memberships.stream()
                .map(m -> UserResponse.builder()
                        .id(m.getUser().getId())
                        .email(m.getUser().getEmail())
                        .nome(m.getUser().getNome())
                        .role(m.getUser().getRole().name())
                        .build())
                .collect(Collectors.toList());
    }

    public List<ProjectResponse> getAllMyProjects() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        List<Project> createdProjects = projectRepository.findByCriador_Email(userEmail);
        List<ProjectMembership> memberships = projectMembershipRepository.findByUserId(user.getId());
        List<Project> memberProjects = memberships.stream()
                .map(ProjectMembership::getProject)
                .collect(Collectors.toList());
        List<Project> allProjects = new java.util.ArrayList<>();
        allProjects.addAll(createdProjects);
        allProjects.addAll(memberProjects);
        return allProjects.stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    // --- Métodos de Gerenciamento de Áudio ---
    public void uploadAudioFile(Long projectId, MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found!"));
        boolean isCreator = project.getCriador().getEmail().equals(userEmail);
        boolean isMember = projectMembershipRepository.findByUserIdAndProjectId(userRepository.findByEmail(userEmail).get().getId(), projectId).isPresent();
        if (!isCreator && !isMember) {
            throw new RuntimeException("Access Denied: You are not authorized to upload to this project.");
        }
        try {
            Path projectAudioDir = Paths.get(BASE_UPLOAD_DIR, AUDIO_DIR_PREFIX, String.valueOf(projectId));
            if (!Files.exists(projectAudioDir)) {
                Files.createDirectories(projectAudioDir);
            }
            String fileName = file.getOriginalFilename();
            Path filePath = projectAudioDir.resolve(fileName);
            file.transferTo(filePath);
            AudioFile audioFile = AudioFile.builder()
                    .fileName(fileName)
                    .filePath(filePath.toString())
                    .mimeType(file.getContentType())
                    .fileSize(file.getSize())
                    .project(project)
                    .build();
            audioFileRepository.save(audioFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload audio file: " + e.getMessage());
        }
    }

    public List<AudioFileResponse> getProjectAudioFiles(Long projectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found!"));
        boolean isCreator = project.getCriador().getEmail().equals(userEmail);
        boolean isMember = projectMembershipRepository.findByUserIdAndProjectId(userRepository.findByEmail(userEmail).get().getId(), projectId).isPresent();
        if (!isCreator && !isMember) {
            throw new RuntimeException("Access Denied: You are not authorized to view files for this project.");
        }
        List<AudioFile> audioFiles = audioFileRepository.findByProjectId(projectId);
        return audioFiles.stream()
                .map(this::mapToAudioFileResponse)
                .collect(Collectors.toList());
    }

    public Resource downloadAudioFile(Long projectId, Long audioId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found!"));
        boolean isCreator = project.getCriador().getEmail().equals(userEmail);
        boolean isMember = projectMembershipRepository.findByUserIdAndProjectId(userRepository.findByEmail(userEmail).get().getId(), projectId).isPresent();
        if (!isCreator && !isMember) {
            throw new RuntimeException("Access Denied: You are not authorized to download files from this project.");
        }
        AudioFile audioFile = audioFileRepository.findByIdAndProjectId(audioId, projectId)
                .orElseThrow(() -> new RuntimeException("Audio file not found or does not belong to the specified project."));
        try {
            Path filePath = Paths.get(audioFile.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found on disk: " + audioFile.getFileName());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading file: " + e.getMessage());
        }
    }

    public void deleteAudioFile(Long projectId, Long audioId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found!"));
        boolean isCreator = project.getCriador().getEmail().equals(userEmail);
        boolean isMember = projectMembershipRepository.findByUserIdAndProjectId(userRepository.findByEmail(userEmail).get().getId(), projectId).isPresent();
        if (!isCreator && !isMember) {
            throw new RuntimeException("Access Denied: You are not authorized to delete files from this project.");
        }
        AudioFile audioFile = audioFileRepository.findByIdAndProjectId(audioId, projectId)
                .orElseThrow(() -> new RuntimeException("Audio file not found or does not belong to the specified project."));
        File fileToDelete = new File(audioFile.getFilePath());
        if (fileToDelete.exists() && fileToDelete.delete()) {
            System.out.println("File deleted successfully from disk.");
        } else {
            System.err.println("Failed to delete file from disk or file does not exist.");
        }
        audioFileRepository.delete(audioFile);
    }

    // --- Métodos Utilitários ---
    private ProjectResponse mapToProjectResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .nome(project.getNome())
                .descricao(project.getDescricao())
                .status(project.getStatus())
                .criadorEmail(project.getCriador().getEmail())
                .dataCriacao(project.getDataCriacao())
                .dataAtualizacao(project.getDataAtualizacao())
                .build();
    }

    private AudioFileResponse mapToAudioFileResponse(AudioFile audioFile) {
        return new AudioFileResponse(
                audioFile.getId(),
                audioFile.getFileName(),
                audioFile.getMimeType(),
                audioFile.getFileSize(),
                audioFile.getUploadedAt()
        );
    }
}