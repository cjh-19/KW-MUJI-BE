package com.muji_backend.kw_muji.common.entity;

import com.muji_backend.kw_muji.common.entity.enums.ProjectType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType projectType;

    @Column(nullable = false)
    private boolean start;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime deadlineAt;

    @PrePersist // entity가 영속화되기 직전에 실행
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        createdAt = now;
    }

    // project : userCalendar = 1 : N
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCalendarEntity> userCalendar = new ArrayList<>();

    // project : participation = 1 : N
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipationEntity> participation = new ArrayList<>();
}