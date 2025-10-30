package com.example.gradproject;

import com.example.gradproject.Repository.UserRepo;
import com.example.gradproject.entity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class GradProjectBackend {

    public static void main(String[] args) {
        SpringApplication.run(GradProjectBackend.class, args);
    }
    /*
    @Bean
    public CommandLineRunner initDatabase(UserRepo userRepository) {
        return args -> {
            if (userRepository.count() == 0) { // only add if no users exist
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

                User user = new User();
                user.setFirstName("Mahmoud");
                user.setLastName("Ahmed");
                user.setEmail("mahmoud@example.com");
                user.setPassword(encoder.encode("123456")); // encrypt password

                userRepository.save(user);
                System.out.println("✅ New user added: " + user);
            } else {
                System.out.println("ℹ️ Users already exist in database, skipping seeding.");
            }
        };
    }
    */

}
