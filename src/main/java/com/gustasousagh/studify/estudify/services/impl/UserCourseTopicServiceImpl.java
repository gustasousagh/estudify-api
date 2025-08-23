package com.gustasousagh.studify.estudify.services.impl;

import com.gustasousagh.studify.estudify.dto.CourseTopicDTO;
import com.gustasousagh.studify.estudify.dto.CourseTopicDetailsDTO;
import com.gustasousagh.studify.estudify.entity.CourseEntity;
import com.gustasousagh.studify.estudify.entity.CourseTopicEntity;
import com.gustasousagh.studify.estudify.integrations.dto.ModuleDTO;
import com.gustasousagh.studify.estudify.repository.CourseTopicRepository;
import com.gustasousagh.studify.estudify.services.UserCourseTopicService;
import com.gustasousagh.studify.estudify.utils.ContentCleaner;
import com.gustasousagh.studify.estudify.utils.GeminiResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCourseTopicServiceImpl implements UserCourseTopicService {

    private final CourseTopicRepository courseTopicRepository;
    private final GeminiResponseParser geminiParser;
    private final ContentCleaner contentCleaner;

    public List<CourseTopicDTO> getAllTopicTitlesByCourseId(Long courseId) {
        List<CourseTopicEntity> topics = courseTopicRepository.findByCourseIdOrderByOrderAsc(courseId);
        return topics.stream()
                .map(topic -> new CourseTopicDTO(topic.getId(), topic.getTopicTitle(), topic.isComplete(), topic.getCreatedAt(), topic.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public CourseTopicDetailsDTO getTopicDetails(Long courseId, Long topicId) {
        CourseTopicEntity topic = courseTopicRepository.findByCourseIdAndId(courseId, topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        return new CourseTopicDetailsDTO(topic.getId(), topic.getTopicTitle(), topic.getTopicDescription(), topic.getTopicContent(), topic.isComplete(), topic.getCreatedAt(), topic.getUpdatedAt());
    }

    @Override
    public CourseTopicEntity buildTopicEntity(CourseEntity courseEntity, ModuleDTO moduleDTO, String contentJson) {
        GeminiResponseParser.ContentExtraction extraction = geminiParser.extractContentBestEffort(contentJson);
        if (!extraction.parsed()) {
            log.info("Conteúdo do módulo '{}' salvo em fallback. Motivo: {}", moduleDTO.getTitle(), extraction.errorMessage());
        }
        CourseTopicEntity topicEntity = new CourseTopicEntity();
        topicEntity.setCourse(courseEntity);
        topicEntity.setTopicTitle(moduleDTO.getTitle());
        topicEntity.setOrder(moduleDTO.getOrder());
        topicEntity.setTopicDescription(moduleDTO.getObjective());
        topicEntity.setTopicContent(contentCleaner.cleanContent(extraction.content()));
        return topicEntity;
    }

    @Override
    public Mono<List<CourseTopicEntity>> persistAll(List<CourseTopicEntity> topics) {
        return Mono.fromCallable(() -> courseTopicRepository.saveAll(topics))
                .subscribeOn(Schedulers.boundedElastic())
                .map(iterable -> topics);
    }

    @Override
    public void completeTopic(Long courseId, Long topicId, Long userId) {
        CourseTopicEntity topic = courseTopicRepository.findByCourseIdAndId(courseId, topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        if (topic.isComplete()) {
            log.warn("Topic {} already completed for user {}", topicId, userId);
            return;
        }

        topic.setComplete(true);
        courseTopicRepository.save(topic);
        log.info("Topic {} marked as complete for user {}", topicId, userId);
    }
}
