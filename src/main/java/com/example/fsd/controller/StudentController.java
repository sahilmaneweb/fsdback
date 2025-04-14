package com.example.fsd.controller;


import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fsd.entity.Batch;
import com.example.fsd.entity.Student;
import com.example.fsd.entity.User;
import com.example.fsd.repository.BatchRepository;
import com.example.fsd.repository.StudentRepository;
import com.example.fsd.repository.UserRepository;
import com.example.fsd.response.ResponseBean;
import com.example.fsd.response.StudentDto.StudentRequest;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class StudentController {
    @Autowired
    public StudentRepository studentRepo;
    @Autowired
    public UserRepository userRepo;
    @Autowired
    public BatchRepository batchRepo;

    @GetMapping(path="/student/profile/{studentUid}",produces="application/json")
    public ResponseEntity<ResponseBean> getStudentById(@PathVariable String studentUid){
        ResponseBean response = new ResponseBean();
        try{
            Student student = studentRepo.findById(studentUid).orElse(null);
            if(student == null){
                response.setMessage("Student not found");
                response.setStatus(false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.setStatus(true);
            response.setMessage("Student Found");
            response.setData(student);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        }catch(Exception e){
            response.setMessage("Error retrieving students: " + e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // Get all students data
    @GetMapping(path = "/student", produces = "application/json")
    public ResponseEntity<ResponseBean> getAllStudents() {
        ResponseBean response = new ResponseBean();
        try {
            Iterable<Student> students = studentRepo.findAll();
            response.setMessage("All Students retrieved successfully !!!!");
            response.setStatus(true);
            response.setData(students);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Error retrieving students: " + e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping(path="/student/{batchName}", produces = "application/json")
    public ResponseEntity<ResponseBean> getStudentsByBatch(@PathVariable String batchName) {
        ResponseBean response = new ResponseBean();
        try {
            Iterable<Student> students = studentRepo.findByBatch_BatchName(batchName);
            response.setMessage("Students of " + batchName + " retrieved successfully");
            response.setStatus(true);
            response.setData(students);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Error retrieving students: " + e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    //Get students by batch name and whose group is null or by group id
    @GetMapping(path="/student/{batchName}/{groupId}",produces="application/json")
    public ResponseEntity<ResponseBean> getStudentsByGroup(@PathVariable String batchName, @PathVariable String groupId) {
        ResponseBean response = new ResponseBean();
        try {
            Iterable<Student> students;
            if (groupId.equals("null")) {
                students = studentRepo.findByBatch_BatchNameAndGroupIsNull(batchName);
            } else {
                students = studentRepo.findByBatchNameAndGroupId(batchName, groupId);
            }
            response.setMessage("Students of batch " + batchName + " &  Group " + groupId + "retrieved successfully");
            response.setStatus(true);
            response.setData(students);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Error retrieving students: " + e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping(path="/student/{batchName}/{groupId}/null",produces="application/json")
    public ResponseEntity<ResponseBean> getStudentsByGroupNull(@PathVariable String batchName, @PathVariable String groupId) {
        ResponseBean response = new ResponseBean();
        try {
            Iterable<Student> students = studentRepo.findByBatchNameAndGroupIsNullOrGroupId(batchName, groupId);
            response.setMessage("Students Batch " + batchName + " & Group null or "+ groupId + " retrieved successfully");
            response.setStatus(true);
            response.setData(students);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Error retrieving students: " + e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Create new student (admin)
    @PostMapping(path = "/admin/student", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ResponseBean> addStudent(@RequestBody StudentRequest studentreq) {
        ResponseBean response = new ResponseBean();
        try {
            Batch batch = batchRepo.findById(studentreq.getBatchName()).orElseThrow(() -> new RuntimeException("Batch not found"));
            
            String uid = generateUniqueUid();
            Student student = new Student();
            student.setUid(uid);
            student.setName(studentreq.getName());
            student.setEmail(studentreq.getEmail());
            student.setPhone(studentreq.getPhone());
            student.setAddress(studentreq.getAddress());
            student.setGender(studentreq.getGender());
            student.setBatch(batch);
            student.setGroup(null);

            Student savedStudent = studentRepo.save(student);

            User user = new User();
            user.setUsername(savedStudent.getUid());
            String plainPassword = savedStudent.getPhone();
            System.out.println(plainPassword);
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
            System.out.println(hashedPassword);
            user.setPassword(hashedPassword);
            user.setRole("student");
            userRepo.save(user);



            response.setMessage("Student added successfully");
            response.setStatus(true);
            response.setData(savedStudent);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.setMessage("Error adding student: " + e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        
    }

    //Update student record
    @PutMapping(path="/admin/student/{studentId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ResponseBean> updateStudentByStudent(@RequestBody StudentRequest student, @PathVariable String studentId) {
        ResponseBean response = new ResponseBean();
        try {
            Student existingStudent = studentRepo.findById(studentId).orElse(null);
            if (existingStudent == null) {
                response.setMessage("Student not found");
                response.setStatus(false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            existingStudent.setName(student.getName());
            existingStudent.setEmail(student.getEmail());
            existingStudent.setPhone(student.getPhone());
            existingStudent.setGender(student.getGender());
            existingStudent.setAddress(student.getAddress());
            

            Student updatedStudent = studentRepo.save(existingStudent);
            response.setMessage("Student updated successfully");
            response.setStatus(true);
            response.setData(updatedStudent);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Error updating student: " + e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PutMapping(path="/student/{studentId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ResponseBean> updateStudent(@RequestBody StudentRequest student, @PathVariable String studentId) {
        ResponseBean response = new ResponseBean();
        try {
            Student existingStudent = studentRepo.findById(studentId).orElse(null);
            if (existingStudent == null) {
                response.setMessage("Student not found");
                response.setStatus(false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            existingStudent.setName(student.getName());
            existingStudent.setEmail(student.getEmail());
            existingStudent.setPhone(student.getPhone());
            existingStudent.setGender(student.getGender());
            existingStudent.setAddress(student.getAddress());
            

            Student updatedStudent = studentRepo.save(existingStudent);
            response.setMessage("Student updated successfully");
            response.setStatus(true);
            response.setData(updatedStudent);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Error updating student: " + e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Delete a Student
    @DeleteMapping(path="/admin/student/{studentId}", produces = "application/json")
    public ResponseEntity<ResponseBean> deleteStudent(@PathVariable String studentId) {
        ResponseBean response = new ResponseBean();
        try {
            Student existingStudent = studentRepo.findById(studentId).orElse(null);
            if (existingStudent == null) {
                response.setMessage("Student not found");
                response.setStatus(false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            User user = userRepo.findByUsername(studentId);
            if (user != null) {
                userRepo.delete(user);
            }
            studentRepo.delete(existingStudent);
            response.setMessage("Student : " + studentId + " deleted successfully");
            response.setStatus(true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Error deleting student: " + e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // Generate a unique UID for the student
    private String generateUniqueUid(){
        String uid;
        do {
            uid = "S" + (int)(Math.random() * 90000 + 1000000);
        } while (studentRepo.existsById(uid));
        return uid;
    }
}
