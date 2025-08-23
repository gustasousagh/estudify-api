package com.gustasousagh.studify.estudify.services;

import com.gustasousagh.studify.estudify.dto.CourseTopicDTO;
import com.gustasousagh.studify.estudify.dto.CourseTopicDetailsDTO;
import com.gustasousagh.studify.estudify.entity.CourseEntity;
import com.gustasousagh.studify.estudify.entity.CourseTopicEntity;
import com.gustasousagh.studify.estudify.integrations.dto.ModuleDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserCourseTopicService {
    List<CourseTopicDTO> getAllTopicTitlesByCourseId(Long courseId);
    CourseTopicDetailsDTO getTopicDetails(Long courseId, Long topicId);
    CourseTopicEntity buildTopicEntity(CourseEntity courseEntity, ModuleDTO moduleDTO, String contentJson);
    Mono<List<CourseTopicEntity>> persistAll(List<CourseTopicEntity> topics);
    void completeTopic(Long courseId, Long topicId, Long userId);
}
