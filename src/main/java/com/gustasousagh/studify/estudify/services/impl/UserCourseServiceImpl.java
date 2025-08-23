package com.gustasousagh.studify.estudify.services.impl;

import com.gustasousagh.studify.estudify.dto.CourseDTO;
import com.gustasousagh.studify.estudify.dto.CourseSummaryDTO;
import com.gustasousagh.studify.estudify.entity.CourseEntity;
import com.gustasousagh.studify.estudify.entity.SubscriptionEntity;
import com.gustasousagh.studify.estudify.enums.CourseType;
import com.gustasousagh.studify.estudify.integrations.client.GeminiClient;
import com.gustasousagh.studify.estudify.model.request.GenerateCourseRequest;
import com.gustasousagh.studify.estudify.projection.CourseListProjection;
import com.gustasousagh.studify.estudify.repository.CourseRepository;
import com.gustasousagh.studify.estudify.repository.SubscriptionRepository;
import com.gustasousagh.studify.estudify.services.UserCourseService;
import com.gustasousagh.studify.estudify.services.UserCourseTopicService;
import com.gustasousagh.studify.estudify.utils.GeminiResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCourseServiceImpl implements UserCourseService {

    private static final Duration GEMINI_TIMEOUT = Duration.ofSeconds(60);

    private final GeminiClient geminiClient;
    private final CourseRepository courseRepository;
    private final UserCourseTopicService courseTopicService;
    private final ModelMapper modelMapper;
    private final GeminiResponseParser geminiParser;
    private final SubscriptionRepository subscriptionRepository;

    @Value("${studify.content.concurrency:21}")
    int perCourseConcurrency;

    @Override
    public Page<CourseDTO> getCoursesByUserId(String userId, Pageable pageable) {
        Page<CourseListProjection> page = courseRepository.findCourseListWithTotals(userId, pageable);
        return page.map(r -> {
            CourseDTO dto = new CourseDTO();
            dto.setId(r.getId());
            dto.setUserId(r.getUserId());
            dto.setCourseTitle(r.getCourseTitle());
            dto.setCourseDescription(r.getCourseDescription());
            dto.setCourseImageUrl(r.getCourseImageUrl());
            dto.setComplete(r.isComplete());
            dto.setType(r.getType());
            dto.setCourseTotalModules(r.getTotalModules());
            dto.setCourseCompletedModules(r.getCompletedModules());
            dto.setCreatedAt(r.getCreatedAt());
            dto.setUpdatedAt(r.getUpdatedAt());
            return dto;
        });
    }

    @Override
    public Mono<CourseDTO> generateAndSaveCourse(GenerateCourseRequest request, String userId) {
        long startMillis = System.currentTimeMillis();
        SubscriptionEntity subscription = subscriptionRepository.findByUserId(userId);

        if (subscription == null || !subscription.getStatus().equals("active")) {
            return Mono.error(new RuntimeException("Usuário não possui uma assinatura ativa."));
        }

        return validateRequest(request, userId)
                .then(Mono.defer(() -> Mono.fromCallable(() -> courseRepository.save(newCourse(request, userId)))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(savedCourse ->
                                geminiClient.generateCourseRoadmap(request)
                                        .timeout(GEMINI_TIMEOUT)
                                        .flatMapMany(geminiParser::extractModules)
                                        .flatMap(moduleDTO ->
                                                        geminiClient.generateCourseContent(moduleDTO.getObjective(), moduleDTO.getTitle())
                                                                .timeout(GEMINI_TIMEOUT)
                                                                .retryWhen(reactor.util.retry.Retry
                                                                        .backoff(2, Duration.ofMillis(200))
                                                                        .filter(this::isRetryable))
                                                                .onErrorReturn(geminiParser.fallbackContentJson())
                                                                .map(contentJson -> courseTopicService
                                                                        .buildTopicEntity(savedCourse, moduleDTO, contentJson)),
                                                perCourseConcurrency)
                                        .collectList()
                                        .flatMap(courseTopicService::persistAll)
                                        .thenReturn(savedCourse)
                        )))
                .map(entity -> modelMapper.map(entity, CourseDTO.class))
                .doFinally(_sig -> log.debug("generateAndSaveCourse levou {} ms", System.currentTimeMillis() - startMillis));
    }

    @Override
    public CourseSummaryDTO getCourseSummary(String userId) {
        long totalCursos = courseRepository.countByUserIdAndType(userId, CourseType.COURSE);
        long totalQuizz = courseRepository.countByUserIdAndType(userId, CourseType.QUIZZ);
        long cursosCompletos = courseRepository.countByUserIdAndComplete(userId, true);
        long cursosIncompletos = courseRepository.countByUserIdAndComplete(userId, false);
        return new CourseSummaryDTO(totalCursos, totalQuizz, cursosCompletos, cursosIncompletos);
    }

    private boolean isRetryable(Throwable throwable) {
        if (throwable instanceof TimeoutException) return true;
        if (throwable instanceof WebClientRequestException requestEx) {
            Throwable cause = requestEx.getCause();
            return (cause instanceof SocketTimeoutException
                    || cause instanceof SocketException
                    || cause instanceof IOException);
        }
        if (throwable instanceof WebClientResponseException responseEx) {
            return responseEx.getStatusCode().is5xxServerError()
                    || responseEx.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
        }
        return false;
    }

    private Mono<Void> validateRequest(GenerateCourseRequest request, String userId) {
        Objects.requireNonNull(userId, "userId não pode ser nulo");
        if (request == null
                || request.getCourseTitle() == null || request.getCourseTitle().isBlank()
                || request.getCourseNumberOfModules() == null || request.getCourseNumberOfModules() <= 0) {
            return Mono.error(new IllegalArgumentException("Requisição inválida"));
        }
        return Mono.empty();
    }

    private CourseEntity newCourse(GenerateCourseRequest request, String userId) {
        CourseEntity courseEntity = new CourseEntity();
        courseEntity.setCourseTitle(request.getCourseTitle().trim());
        courseEntity.setCourseDescription(request.getCourseDescription());
        courseEntity.setCourseTotalModules(request.getCourseNumberOfModules());
        courseEntity.setCourseImageUrl("https://imgs.search.brave.com/Q_STddr324SPg6QYUsf2TF50rawjZrQdMLtrSH1aXIo/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9jZG4t/aWNvbnMtcG5nLmZy/ZWVwaWsuY29tLzI1/Ni8xMTkxOC8xMTkx/ODY3OS5wbmc_c2Vt/dD1haXNfd2hpdGVf/bGFiZWw");
        courseEntity.setUserId(userId);
        return courseEntity;
    }
}
