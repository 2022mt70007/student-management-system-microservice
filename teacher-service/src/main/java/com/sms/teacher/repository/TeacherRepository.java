package com.sms.teacher.repository;

import com.sms.teacher.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByEmail(String email);
    boolean existsByEmail(String email);

    List<Teacher> findByDepartmentIdAndClassIdOrderByNameAsc(Long departmentId, Long classId);
}
