package com.gustasousagh.studify.estudify.repository;

import com.gustasousagh.studify.estudify.entity.CourseEntity;
import com.gustasousagh.studify.estudify.enums.CourseType;
import com.gustasousagh.studify.estudify.projection.CourseListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<CourseEntity, Long> {
    Page<CourseEntity> findByUserId(String userId, Pageable pageable);
    long countByUserId(String userId);
    long countByUserIdAndType(String userId, CourseType type);
    long countByUserIdAndComplete(String userId, boolean complete);

    @Query(
            value = """
        select
          c.id as id,
          c.userId as userId,
          c.courseTitle as courseTitle,
          c.courseDescription as courseDescription,
          c.courseImageUrl as courseImageUrl,
          c.complete as complete,
          c.type as type,
          c.createdAt as createdAt,
          c.updatedAt as updatedAt,
          count(distinct ct.id) as totalModules,
          coalesce(sum(case when ct.complete = true then 1 else 0 end), 0) as completedModules
        from CourseEntity c
        left join c.courseTopics ct
        where (:userId is null or c.userId = :userId)
        group by c.id, c.userId, c.courseTitle, c.courseDescription, c.courseImageUrl,
                 c.complete, c.type
                 order by c.createdAt desc
      """,
            countQuery = """
        select count(c)
        from CourseEntity c
        where (:userId is null or c.userId = :userId)
      """
    )
    Page<CourseListProjection> findCourseListWithTotals(@Param("userId") String userId, Pageable pageable);
}
