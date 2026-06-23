package com.sms.course.repository;

import com.sms.course.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByClassIdOrderBySubjectNameAsc(Long classId);
    List<Subject> findByClassIdInOrderBySubjectNameAsc(List<Long> classIds);
    boolean existsByClassIdAndSubjectCode(Long classId, String subjectCode);
    boolean existsByClassIdAndSubjectName(Long classId, String subjectName);
    boolean existsByClassIdAndSubjectCodeAndIdNot(Long classId, String subjectCode, Long id);
    boolean existsByClassIdAndSubjectNameAndIdNot(Long classId, String subjectName, Long id);
    long countByClassId(Long classId);
}
