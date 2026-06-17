package com.sms.student.repository;

import com.sms.student.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByCourseIdOrderByScheduledDateAsc(Long courseId);

    List<Exam> findByCourseIdInOrderByScheduledDateAsc(List<Long> courseIds);
}
