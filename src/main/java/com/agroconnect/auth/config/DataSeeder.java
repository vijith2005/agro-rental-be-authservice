package com.agroconnect.auth.config;

import static java.lang.Math.log;

import com.agroconnect.auth.entity.Role;
import com.agroconnect.auth.entity.User;
import com.agroconnect.auth.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            String adminEmail = "admin@agroconnect.com";
            if (userRepository.existsByEmail(adminEmail)) {
                return;
            }
            User admin = new User();
            admin.setName("Agro Admin");
            admin.setEmail(adminEmail);
            admin.setPhone("9999999999");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
        } catch (Exception ex) {
            System.out.println("Skipping admin seed due to startup data issue: " + ex.getMessage());
        }
    }
}
