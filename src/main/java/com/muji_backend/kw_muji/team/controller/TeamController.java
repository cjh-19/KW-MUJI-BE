package com.muji_backend.kw_muji.team.controller;

import com.muji_backend.kw_muji.common.entity.ParticipationEntity;
import com.muji_backend.kw_muji.common.entity.ProjectEntity;
import com.muji_backend.kw_muji.common.entity.ResumeEntity;
import com.muji_backend.kw_muji.common.entity.UserEntity;
import com.muji_backend.kw_muji.common.entity.enums.ProjectRole;
import com.muji_backend.kw_muji.team.dto.request.RegisterRequestDTO;
import com.muji_backend.kw_muji.team.dto.request.ResumeRequestDTO;
import com.muji_backend.kw_muji.team.dto.response.ProjectDetailResponseDTO;
import com.muji_backend.kw_muji.team.dto.response.ResumeListResponseDTO;
import com.muji_backend.kw_muji.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/team")
public class TeamController {
    private final TeamService teamService;

    @Value("${cloud.aws.s3.url}")
    private String bucketURL;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> writeProject(
            @AuthenticationPrincipal UserEntity userInfo,
            @ModelAttribute RegisterRequestDTO dto, BindingResult bindingResult) {
        try {
            // 유효성 검사
            teamService.validation(bindingResult, "name");
            teamService.validation(bindingResult, "description");

            final ProjectEntity teamProject = new ProjectEntity();

            if (dto.getImage() != null && dto.getImage().length > 0 && !dto.getImage()[0].isEmpty()) {
                teamProject.setImage(teamService.uploadProjectImage(dto.getImage(), dto.getName()));
            }

            teamProject.setName(dto.getName());
            teamProject.setDescription(dto.getDescription());
            teamProject.setDeadlineAt(dto.getDeadlineAt().atStartOfDay());

            final ParticipationEntity participation = ParticipationEntity.builder()
                    .project(teamProject)
                    .role(ProjectRole.CREATOR)
                    .users(userInfo)
                    .build();

            if (teamProject.getParticipation() == null) {
                teamProject.setParticipation(new ArrayList<>());
            }
            teamProject.getParticipation().add(participation);

            teamService.registerProject(teamProject);

            return ResponseEntity.ok().body(Map.of("code", 200, "data", true));
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "data", "팀플 모집 글쓰기 오류. 잠시 후 다시 시도해주세요."));
        }
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Map<String, Object>> getProject(@AuthenticationPrincipal UserEntity userInfo, @PathVariable Long projectId) {
        try {
            final ProjectEntity project = teamService.getProject(projectId);

            final ProjectDetailResponseDTO resDTO = ProjectDetailResponseDTO.builder()
                    .name(project.getName())
                    .description(project.getDescription())
                    .createdAt(project.getCreatedAt())
                    .deadlineAt(LocalDate.from(project.getDeadlineAt()))
                    .image(project.getImage() != null ? bucketURL + URLEncoder.encode(project.getImage(), "UTF-8") : "")
                    .role(teamService.getRole(projectId, userInfo) == null ? null : teamService.getRole(projectId, userInfo).getRole())
                    .isOnGoing(project.isOnGoing())
                    .start(project.isStart())
                    .build();

            return ResponseEntity.ok().body(Map.of("code", 200, "data", resDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", e.getMessage()));
        }
    }

    @GetMapping("/apply")
    public ResponseEntity<Map<String, Object>> getResumeList(@AuthenticationPrincipal UserEntity userInfo) {
        try {
            final ResumeListResponseDTO resDTO = ResumeListResponseDTO.builder()
                    .resumes(teamService.getAllResumes(userInfo))
                    .build();

            return ResponseEntity.ok().body(Map.of("code", 200, "data", resDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", e.getMessage()));
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyProject(@AuthenticationPrincipal UserEntity userInfo, @RequestBody ResumeRequestDTO dto) {
        try {
            final ProjectEntity project = teamService.getProject(dto.getProjectId());
            final ResumeEntity resume = teamService.getResume(dto.getResumeId());

            if(!Objects.equals(resume.getUsers().getId(), userInfo.getId()))
                throw new IllegalArgumentException("본인 포트폴리오가 아님");

            final ParticipationEntity participation = ParticipationEntity.builder()
                    .project(project)
                    .role(ProjectRole.APPLICANT)
                    .resumePath(resume.getResumePath())
                    .users(userInfo)
                    .build();

            teamService.saveParticipation(participation, project);

            return ResponseEntity.ok().body(Map.of("code", 200, "data", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> showAllProjects(@RequestParam(value = "page", defaultValue = "0") int page,
                                                               @RequestParam(value = "search", required = false) String search) {
        try {
            Map<String, Object> result = teamService.getOnGoingProjects(page, search);

            return ResponseEntity.ok().body(Map.of("code", 200, "data", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", e.getMessage()));
        }
    }
}
