package com.sms.admin.service;

import com.sms.admin.client.*;
import com.sms.common.dto.*;
import com.sms.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminOrchestrationService {

    private final StudentClient studentClient;
    private final TeacherClient teacherClient;
    private final AdminProfileService adminProfileService;
    private final CourseClient courseClient;
    private final NotificationClient notificationClient;
    private final AuthClient authClient;
    private final EmailService emailService;

    public StudentResponse createStudent(StudentRequest request) {
        StudentResponse student = studentClient.create(request).getData();

        CreateInvitationRequest invitationRequest = new CreateInvitationRequest();
        invitationRequest.setEmail(student.getEmail());
        invitationRequest.setRole(UserRole.STUDENT);
        invitationRequest.setProfileId(student.getId());

        InvitationResponse invitation = authClient.createInvitation(invitationRequest).getData();
        emailService.sendRegistrationEmail(student.getEmail(), student.getName(), UserRole.STUDENT, invitation);

        return student;
    }

    public StudentResponse updateStudent(Long id, StudentRequest request) {
        return studentClient.update(id, request).getData();
    }

    public void deleteStudent(Long id) {
        studentClient.delete(id);
    }

    public List<StudentResponse> listStudents() {
        return studentClient.findAll().getData();
    }

    public TeacherResponse createTeacher(TeacherRequest request) {
        TeacherResponse teacher = teacherClient.create(request).getData();

        CreateInvitationRequest invitationRequest = new CreateInvitationRequest();
        invitationRequest.setEmail(teacher.getEmail());
        invitationRequest.setRole(UserRole.TEACHER);
        invitationRequest.setProfileId(teacher.getId());

        InvitationResponse invitation = authClient.createInvitation(invitationRequest).getData();
        emailService.sendRegistrationEmail(teacher.getEmail(), teacher.getName(), UserRole.TEACHER, invitation);

        return teacher;
    }

    public TeacherResponse updateTeacher(Long id, TeacherRequest request) {
        return teacherClient.update(id, request).getData();
    }

    public void deleteTeacher(Long id) {
        teacherClient.delete(id);
    }

    public List<TeacherResponse> listTeachers() {
        return teacherClient.findAll().getData();
    }

    public AdminUserResponse createAdmin(AdminUserRequest request) {
        return adminProfileService.create(request);
    }

    public AdminUserResponse updateAdmin(Long id, AdminUserRequest request) {
        return adminProfileService.update(id, request);
    }

    public void deleteAdmin(Long id) {
        adminProfileService.delete(id);
    }

    public List<AdminUserResponse> listAdmins() {
        return adminProfileService.findAll();
    }

    public CourseResponse createCourse(CourseRequest request) {
        return courseClient.create(request).getData();
    }

    public CourseResponse updateCourse(Long id, CourseRequest request) {
        return courseClient.update(id, request).getData();
    }

    public void deleteCourse(Long id) {
        courseClient.delete(id);
    }

    public List<CourseResponse> listCourses() {
        return courseClient.findAll().getData();
    }

    public NotificationResponse createNotification(NotificationRequest request) {
        return notificationClient.create(request).getData();
    }

    public NotificationResponse updateNotification(Long id, NotificationRequest request) {
        return notificationClient.update(id, request).getData();
    }

    public void deleteNotification(Long id) {
        notificationClient.delete(id);
    }

    public List<NotificationResponse> listNotifications() {
        return notificationClient.findAll().getData();
    }

    public void activateAdmin(Long id) {
        adminProfileService.activate(id);
    }

    public Map<String, Object> dashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("students", listStudents());
        dashboard.put("teachers", listTeachers());
        dashboard.put("admins", listAdmins());
        dashboard.put("courses", listCourses());
        dashboard.put("notifications", listNotifications());
        return dashboard;
    }
}
