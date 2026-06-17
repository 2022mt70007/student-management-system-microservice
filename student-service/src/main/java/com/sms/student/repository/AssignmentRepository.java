package com.sms.student.repository;

import com.sms.student.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByCourseIdOrderByDueDateAsc(Long courseId);

    List<Assignment> findByCourseIdInOrderByDueDateAsc(List<Long> courseIds);
}
