package com.sms.student.repository;

import com.sms.student.entity.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {
    List<StudentProgress> findByStudentId(Long studentId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}
