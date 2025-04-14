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
import com.example.fsd.entity.Mentor;
import com.example.fsd.entity.User;
import com.example.fsd.repository.BatchRepository;
import com.example.fsd.repository.MentorRepository;
import com.example.fsd.repository.UserRepository;
import com.example.fsd.response.ResponseBean;
import com.example.fsd.response.MentorDto.MentorRequest;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class MentorController {
    @Autowired
    public MentorRepository mentorRepo;
    @Autowired
    public BatchRepository batchRepo;
    @Autowired
    public UserRepository userRepo;
    
    // Get all mentors data
    @GetMapping(path = "admin/mentor", produces = "application/json")
    public ResponseEntity<ResponseBean> getAllMentors() {
        ResponseBean response = new ResponseBean();
        try {
            Iterable<Mentor> mentors = mentorRepo.findAll();
            response.setMessage("Mentors retrieved successfully");
            response.setStatus(true);
            response.setData(mentors);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Error retrieving mentors: " + e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //Get mentor data
    @GetMapping(path = "admin/mentor/{mentorId}", produces = "application/json")
    public ResponseEntity<ResponseBean> getMentor(@PathVariable String mentorId) {
        ResponseBean response = new ResponseBean();
        try {
            Mentor mentor = mentorRepo.findById(mentorId).orElseThrow(() -> new RuntimeException("Mentor not found"));
            response.setMessage("Mentor retrieved successfully");
            response.setStatus(true);
            response.setData(mentor);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Error retrieving mentor: " + e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Create new mentor (admin)
    @PostMapping(path = "admin/mentor", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ResponseBean> addMentor(@RequestBody MentorRequest mentorreq) {
        ResponseBean response = new ResponseBean();
        try {
            String uid = generateUniqueUid();
            Batch batch = batchRepo.findById(mentorreq.getBatchName()).orElseThrow(() -> new RuntimeException("Batch not found"));

            Mentor mentor = new Mentor();
            mentor.setMentorId(uid);
            mentor.setName(mentorreq.getName());
            mentor.setEmail(mentorreq.getEmail());
            mentor.setContact(mentorreq.getContact());
            mentor.setGender(mentorreq.getGender());
            mentor.setBatch(batch);
            mentor.setDepartment(mentorreq.getDepartment());
            mentor.setSpecialization(mentorreq.getSpecialization());

            Mentor savedMentor = mentorRepo.save(mentor);

            User user = new User();
            user.setUsername(savedMentor.getMentorId());
            String plainPassword = savedMentor.getContact();
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
            user.setPassword(hashedPassword);


            user.setRole("Mentor");
            userRepo.save(user);
            response.setMessage("Mentor added successfully");
            response.setStatus(true);
            response.setData(savedMentor);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.setMessage("Error adding mentor: " + e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //Update mentor details
    @PutMapping(path = "admin/mentor/{mentorId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ResponseBean> updateMentor(@RequestBody MentorRequest mentorreq, @PathVariable String mentorId) {
        ResponseBean response = new ResponseBean();
        try {
            Mentor mentor = mentorRepo.findById(mentorId).orElseThrow(() -> new RuntimeException("Mentor not found"));
            Batch batch = batchRepo.findById(mentorreq.getBatchName()).orElseThrow(() -> new RuntimeException("Batch not found"));

            mentor.setName(mentorreq.getName());
            mentor.setEmail(mentorreq.getEmail());
            mentor.setContact(mentorreq.getContact());
            mentor.setGender(mentorreq.getGender());
            mentor.setBatch(batch);
            mentor.setDepartment(mentorreq.getDepartment());
            mentor.setSpecialization(mentorreq.getSpecialization());

            Mentor updatedMentor = mentorRepo.save(mentor);
            response.setMessage("Mentor updated successfully");
            response.setStatus(true);
            response.setData(updatedMentor);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Error updating mentor: " + e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //Delete mentor
    @DeleteMapping(path = "admin/mentor/{mentorId}", produces = "application/json")
    public ResponseEntity<ResponseBean> deleteMentor(@PathVariable String mentorId) {
        ResponseBean response = new ResponseBean();
        try {
            Mentor mentor = mentorRepo.findById(mentorId).orElseThrow(() -> new RuntimeException("Mentor not found"));

            User user = userRepo.findByUsername(mentor.getMentorId());
            if (user != null) {
                userRepo.delete(user);
            }
            mentorRepo.delete(mentor);
            response.setMessage("Mentor id : " + mentorId + " deleted successfully");
            response.setStatus(true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Error deleting mentor: " + e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    private String generateUniqueUid(){
        String mentorId;
        do {
            mentorId = "M" + (int)(Math.random() * 90000 + 1000000);
        } while (mentorRepo.existsById(mentorId));
        return mentorId;
    }
}
