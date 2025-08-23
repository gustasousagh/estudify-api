package com.gustasousagh.studify.estudify.entity;

import com.gustasousagh.studify.estudify.enums.CourseStatus;
import com.gustasousagh.studify.estudify.enums.CourseType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course")
public class CourseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "course_title")
    private String courseTitle;

    @Column(name = "course_description", columnDefinition = "TEXT")
    private String courseDescription;

    @Column(name = "course_image_url")
    private String courseImageUrl;

    @Column(name = "course_total_modules")
    private Integer courseTotalModules;

    @Column(name = "complete", nullable = false)
    private boolean complete = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CourseType type = CourseType.COURSE;

    @OneToMany(
            mappedBy = "course",
            fetch     = FetchType.LAZY,
            cascade   = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CourseTopicEntity> courseTopics = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

