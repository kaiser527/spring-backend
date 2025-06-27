package com.kaiser.spring_backend.configuration;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.kaiser.spring_backend.entities.User;
import com.kaiser.spring_backend.repositories.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@ConditionalOnProperty(
    prefix = "spring",
    value = "datasource.driverClassName",
    havingValue = "com.mysql.cj.jdbc.Driver"
)
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository){
        return args -> {
            boolean check = userRepository.findByEmail("admin@gmail.com").isEmpty();
            if(check){
                User user =  User.builder()
                    .username("Admin")
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .isActive(true)
                    .image("default.png")
                    .accountType("LOCAL")
                    .build();

                userRepository.save(user);
            }
        };
    }
}
