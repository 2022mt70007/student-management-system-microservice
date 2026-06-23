package com.sms.course.repository;

import com.sms.course.entity.AcademicClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicClassRepository extends JpaRepository<AcademicClass, Long> {
    List<AcademicClass> findByDepartmentIdOrderByClassNameAsc(Long departmentId);
    boolean existsByDepartmentIdAndClassCode(Long departmentId, String classCode);
    boolean existsByDepartmentIdAndClassName(Long departmentId, String className);
    boolean existsByDepartmentIdAndClassCodeAndIdNot(Long departmentId, String classCode, Long id);
    boolean existsByDepartmentIdAndClassNameAndIdNot(Long departmentId, String className, Long id);
    long countByDepartmentId(Long departmentId);
}
