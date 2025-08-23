package com.gustasousagh.studify.estudify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "course_topic")
public class CourseTopicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    @JsonIgnore
    private CourseEntity course;

    @Column(name = "order_number")
    private Integer order;

    @Column(name = "topic_title")
    private String topicTitle;

    @Column(name = "complete", nullable = false)
    private boolean complete = false;

    @Column(name = "topic_description")
    private String topicDescription;

    @Column(name = "topic_content", columnDefinition = "TEXT")
    private String topicContent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

