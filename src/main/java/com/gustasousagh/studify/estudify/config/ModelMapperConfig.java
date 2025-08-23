package com.gustasousagh.studify.estudify.config;

import com.gustasousagh.studify.estudify.dto.CourseDTO;
import com.gustasousagh.studify.estudify.entity.CourseEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STANDARD);

        mapper.typeMap(CourseEntity.class, CourseDTO.class)
                .addMapping(CourseEntity::getId, CourseDTO::setId)
                .addMapping(CourseEntity::getUserId, CourseDTO::setUserId)
                .addMapping(CourseEntity::getCourseTitle, CourseDTO::setCourseTitle)
                .addMapping(CourseEntity::getCourseDescription, CourseDTO::setCourseDescription)
                .addMapping(CourseEntity::getCourseImageUrl, CourseDTO::setCourseImageUrl);

        return mapper;
    }
}
