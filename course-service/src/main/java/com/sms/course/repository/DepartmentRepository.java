package com.sms.course.repository;

import com.sms.common.enums.AcademicStatus;
import com.sms.course.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByDepartmentCode(String departmentCode);
    boolean existsByDepartmentName(String departmentName);
    boolean existsByDepartmentCodeAndIdNot(String departmentCode, Long id);
    boolean existsByDepartmentNameAndIdNot(String departmentName, Long id);
    List<Department> findByStatusOrderByDepartmentNameAsc(AcademicStatus status);
}
