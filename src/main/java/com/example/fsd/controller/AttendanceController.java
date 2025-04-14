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

import com.example.fsd.entity.Attendance;
import com.example.fsd.entity.Batch;
import com.example.fsd.entity.Student;
import com.example.fsd.entity.Attendance.AttendanceStatus;
import com.example.fsd.repository.AttendanceRepository;
import com.example.fsd.repository.BatchRepository;
import com.example.fsd.repository.StudentRepository;
import com.example.fsd.response.ResponseBean;
import com.example.fsd.response.AttendanceDto.AttendanceDTO;
import com.example.fsd.response.AttendanceDto.AttendanceListDTO;

import org.springframework.web.bind.annotation.CrossOrigin;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

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

    @GetMapping(path="/", produces = "application/json")
    public ResponseEntity<ResponseBean> getAllRecords(){
        ResponseBean response = new ResponseBean();
        try {
            List<Attendance> attendanceList = attendanceRepo.findAll();
            if (attendanceList.isEmpty()) {
                response.setStatus(false);
                response.setMessage("No attendance records found.");
                return ResponseEntity.ok(response);
            }
            response.setStatus(true);
            response.setMessage("Attendance records fetched successfully.");
            response.setData(attendanceList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setStatus(false);
            response.setMessage("Error fetching attendance records: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

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
   
    @PostMapping(path = "/mark", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ResponseBean> markAttendance(@RequestBody AttendanceListDTO request) {
        System.out.println("Received AttendanceDTO: " + request); // Debug statement
        ResponseBean response = new ResponseBean();
        if (request == null) {
            response.setStatus(false);
            response.setMessage("Invalid attendance data provided.");
            return ResponseEntity.badRequest().body(response);
        }
        List<AttendanceDTO> attendanceDTOs = request.getAttendanceList();
        
        try {
            for (AttendanceDTO attendanceDTO : attendanceDTOs) {
                String uid = attendanceDTO.getUid();
                LocalDate date = LocalDate.parse(attendanceDTO.getDate());
                AttendanceStatus status = attendanceDTO.getStatus();
                Optional<Attendance> existingAttendanceOpt = attendanceRepo.findByStudent_UidAndDate(uid, date);
                if (existingAttendanceOpt.isPresent()) {
                    Attendance existingAttendance = existingAttendanceOpt.get();
                    existingAttendance.setStatus(status);
                    attendanceRepo.save(existingAttendance);
                    continue; // Skip to the next record if already exists
                }
                Optional<Student> studentOpt = studentRepo.findById(uid);
                if (studentOpt.isPresent()) {
                    Student student = studentOpt.get();
                    Attendance attendance = new Attendance(null, student, date, status);
                    attendanceRepo.save(attendance);
                } else {
                    continue; // Skip if student not found
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


// [
//   {
//     "uid": "S123",
//     "date": "2025-04-10",
//     "status": "PRESENT"
//   },
//   {
//     "uid": "S124",
//     "date": "2025-04-10",
//     "status": "ABSENT"
//   }
// ]
