package com.example.fsd.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestBody;

import com.example.fsd.entity.Attendance;
import com.example.fsd.entity.Batch;
import com.example.fsd.entity.Student;
import com.example.fsd.entity.Attendance.AttendanceStatus;
import com.example.fsd.repository.AttendanceRepository;
import com.example.fsd.repository.BatchRepository;
import com.example.fsd.repository.StudentRepository;
import com.example.fsd.response.ResponseBean;
import com.example.fsd.response.AttendanceDto.AttendanceDTO;

import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired 
    public AttendanceRepository attendanceRepo;
    @Autowired
    public StudentRepository studentRepo;
    @Autowired
    public BatchRepository batchRepo;

    // Get all attendance records
    @GetMapping("/")
    public ResponseEntity<ResponseBean> getAttendance() {
        List<Attendance> all = attendanceRepo.findAll();

        ResponseBean response = new ResponseBean();
        if (all.isEmpty()) {
            response.setStatus(false);
            response.setMessage("No attendance records found.");
            return ResponseEntity.ok(response);
        }

        response.setStatus(true);
        response.setMessage("Attendance records fetched successfully.");
        response.setData(all);
        return ResponseEntity.ok(response);
    }

    // Get attendance by month and batch
    @GetMapping(path="/{batchName}/{month}", produces = "application/json")
    public ResponseEntity<ResponseBean> getAttendanceByMonthAndBatch(@PathVariable String batchName, @PathVariable LocalDate month){
        ResponseBean response = new ResponseBean();
        try {

            YearMonth yearMonth = YearMonth.from(month);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();

            Optional<Batch> batchObj = batchRepo.findById(batchName);
            List<Attendance> attendanceList = attendanceRepo.findByDateBetweenAndStudent_Batch_BatchName(startDate, endDate, batchObj);
            if (attendanceList.isEmpty()) {
                response.setStatus(false);
                response.setMessage("No attendance records found for the specified month and batch.");
                return ResponseEntity.ok(response);
            }

            Map<String, List<Attendance>> attendanceMap = new HashMap<>();
            for (Attendance attendance : attendanceList) {
                String uid = attendance.getStudent().getUid();
                attendanceMap.computeIfAbsent(uid, k -> new java.util.ArrayList<>()).add(attendance);
            }

            response.setStatus(true);
            response.setMessage("Attendance records fetched successfully.");
            response.setData(attendanceMap);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.setStatus(false);
            response.setMessage("Error fetching attendance records: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Mark attendance for a list of students
    @PostMapping(path = "/mark", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ResponseBean> markAttendance(@RequestBody List<AttendanceDTO> attendanceDTOs) {
        System.out.println("Received AttendanceDTO List: " + attendanceDTOs); // Debug

        ResponseBean response = new ResponseBean();

        if (attendanceDTOs == null || attendanceDTOs.isEmpty()) {
            response.setStatus(false);
            response.setMessage("Invalid attendance data provided.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            for (AttendanceDTO attendanceDTO : attendanceDTOs) {
                String uid = attendanceDTO.getUid();
                LocalDate date = LocalDate.parse(attendanceDTO.getDate()); // Ensure the date is parsed correctly
                AttendanceStatus status = attendanceDTO.getStatus();

                Optional<Attendance> existingAttendanceOpt = attendanceRepo.findByStudent_UidAndDate(uid, date);
                if (existingAttendanceOpt.isPresent()) {
                    Attendance existingAttendance = existingAttendanceOpt.get();
                    existingAttendance.setStatus(status);  // Update status
                    attendanceRepo.save(existingAttendance);
                    continue; // Skip to the next record if already exists
                }

                Optional<Student> studentOpt = studentRepo.findById(uid);
                if (studentOpt.isPresent()) {
                    Student student = studentOpt.get();
                    Attendance attendance = new Attendance(null, student, date, status);
                    attendanceRepo.save(attendance);
                } else {
                    // Student not found, continue with next record
                    System.out.println("Student not found: " + uid);
                    continue;
                }
            }
            response.setStatus(true);
            response.setMessage("Attendance marked successfully.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setStatus(false);
            response.setMessage("Error marking attendance: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}