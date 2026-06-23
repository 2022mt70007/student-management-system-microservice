package com.sms.course.config;

import com.sms.common.enums.AcademicStatus;
import com.sms.course.entity.AcademicClass;
import com.sms.course.entity.Department;
import com.sms.course.entity.Subject;
import com.sms.course.repository.AcademicClassRepository;
import com.sms.course.repository.DepartmentRepository;
import com.sms.course.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AcademicBootstrapConfig {

    private final DepartmentRepository departmentRepository;
    private final AcademicClassRepository classRepository;
    private final SubjectRepository subjectRepository;

    @Bean
    CommandLineRunner seedAcademicStructure() {
        return args -> {
            if (departmentRepository.count() > 0) {
                return;
            }

            Department cs = departmentRepository.save(Department.builder()
                    .departmentCode("CS")
                    .departmentName("Computer Science")
                    .description("Computer Science and Engineering")
                    .status(AcademicStatus.ACTIVE)
                    .build());
            Department ece = departmentRepository.save(Department.builder()
                    .departmentCode("ECE")
                    .departmentName("Electronics")
                    .description("Electronics and Communication Engineering")
                    .status(AcademicStatus.ACTIVE)
                    .build());

            AcademicClass csY1 = classRepository.save(AcademicClass.builder()
                    .classCode("CS-Y1")
                    .className("B.Tech 1st Year")
                    .departmentId(cs.getId())
                    .description("First year Computer Science")
                    .build());
            AcademicClass eceY1 = classRepository.save(AcademicClass.builder()
                    .classCode("ECE-Y1")
                    .className("B.Tech 1st Year")
                    .departmentId(ece.getId())
                    .description("First year Electronics")
                    .build());

            subjectRepository.save(Subject.builder()
                    .subjectCode("CS-PF")
                    .subjectName("Programming Fundamentals")
                    .classId(csY1.getId())
                    .credits(4)
                    .description("Introductory programming course")
                    .build());
            subjectRepository.save(Subject.builder()
                    .subjectCode("CS-DB")
                    .subjectName("Database Management Systems")
                    .classId(csY1.getId())
                    .credits(3)
                    .build());
            subjectRepository.save(Subject.builder()
                    .subjectCode("CS-OS")
                    .subjectName("Operating Systems")
                    .classId(csY1.getId())
                    .credits(3)
                    .build());
            subjectRepository.save(Subject.builder()
                    .subjectCode("ECE-CT")
                    .subjectName("Circuit Theory")
                    .classId(eceY1.getId())
                    .credits(3)
                    .build());
            subjectRepository.save(Subject.builder()
                    .subjectCode("ECE-DE")
                    .subjectName("Digital Electronics")
                    .classId(eceY1.getId())
                    .credits(3)
                    .build());
        };
    }
}
