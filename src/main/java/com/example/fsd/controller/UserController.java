package com.example.fsd.controller;

import java.util.HashMap;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fsd.entity.User;
import com.example.fsd.repository.UserRepository;
import com.example.fsd.response.ResponseBean;
import com.example.fsd.response.UserDto.UpdateUser;
import com.example.fsd.response.UserDto.UserLogin;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    public UserRepository userRepository;

    //Get all users data
    @GetMapping(path = "admin/user", produces = "application/json")
    public ResponseEntity<ResponseBean> getAllUsers() {
        ResponseBean response = new ResponseBean();
        try {
            Iterable<User> users = userRepository.findAll();
            response.setMessage("Users retrieved successfully");
            response.setStatus(true);
            response.setData(users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Error retrieving users: " + e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    //User Login
    @PostMapping(path = "/login",  consumes = "application/json", produces = "application/json")
    public ResponseEntity<ResponseBean> userLoginEntity(@RequestBody UserLogin user){
        ResponseBean response = new ResponseBean();
        try {
            User existing = userRepository.findByUsername(user.getUsername());
            if(existing == null){
                response.setMessage("User Not Found");
                response.setStatus(false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            String loginPassword = user.getPassword();
            String hashedPassword = existing.getPassword();
            boolean isMatch = BCrypt.checkpw(loginPassword, hashedPassword);
            System.out.println(loginPassword + " " + hashedPassword + " "  + isMatch);
            
            if(isMatch){
                Map<String, Object> data = new HashMap<>();
                data.put("role", existing.getRole());
                data.put("username",existing.getUsername());
                data.put("token","Bearer jwt-sample-token");
                response.setMessage("Login Successful");
                response.setStatus(true);
                response.setData(data);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }else{
                response.setMessage("Invalid Password");
                response.setStatus(false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.setMessage("Error happened : "+ e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping(path="/updatePassword", consumes="aplication/json", produces="application/json")
    public ResponseEntity<ResponseBean> updatePassword(@RequestBody UpdateUser user){
        ResponseBean response = new ResponseBean();
        try {
            User existing = userRepository.findByUsername(user.getUsername());
            if(existing == null){
                response.setMessage("User Not Found");
                response.setStatus(false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            String loginPassword = user.getPassword();
            String hashedPassword = existing.getPassword();
            boolean isMatch = BCrypt.checkpw(loginPassword, hashedPassword);
            
            if(isMatch){
                String newHashedPassword = BCrypt.hashpw(user.getNewPassword(), BCrypt.gensalt());
                existing.setPassword(newHashedPassword);
                userRepository.save(existing);
                response.setMessage("Password Updated Successfully");
                response.setStatus(true);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }else{
                response.setMessage("Invalid Password");
                response.setStatus(false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            response.setMessage("Error happened : "+ e.getMessage());
            response.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping(path = "/hello")
    public Boolean helloWorld(){
        String original = "Sahil";
        String hashed = BCrypt.hashpw(original, BCrypt.gensalt());
        System.out.println(hashed);
        boolean matched = BCrypt.checkpw(original, hashed);
        return matched;
    }
    
}
