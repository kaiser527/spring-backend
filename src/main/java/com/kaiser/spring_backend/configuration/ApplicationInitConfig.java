package com.kaiser.spring_backend.configuration;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.kaiser.spring_backend.entities.Permission;
import com.kaiser.spring_backend.entities.Role;
import com.kaiser.spring_backend.entities.User;
import com.kaiser.spring_backend.enums.AccountType;
import com.kaiser.spring_backend.enums.ApiMethod;
import com.kaiser.spring_backend.repositories.PermissionRepository;
import com.kaiser.spring_backend.repositories.RoleRepository;
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
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository, PermissionRepository permissionRepository){
        return args -> {
            long userCount = userRepository.count();
            long roleCount = roleRepository.count();
            long permissionCount = permissionRepository.count();
            boolean check = userCount == 0 && roleCount == 0 && permissionCount == 0;
            if(check){
                //init permission
                //user
                Permission getUser = permissionRepository.save(Permission.builder()
                    .apiPath("/api/v1/user")
                    .method(ApiMethod.GET)
                    .name("Get user Fetch user paginate")
                    .module("USER")
                    .build());
                
                Permission createUser = permissionRepository.save(Permission.builder()
                    .apiPath("/api/v1/user")
                    .method(ApiMethod.POST)
                    .name("Create user")
                    .module("USER")
                    .build());

                Permission updateUser = permissionRepository.save(Permission.builder()
                    .apiPath("/api/v1/user/:id")
                    .method(ApiMethod.PATCH)
                    .name("Update user")
                    .module("USER")
                    .build());

                Permission deleteUser = permissionRepository.save(Permission.builder()
                    .apiPath("/api/v1/user/:id")
                    .method(ApiMethod.DELETE)
                    .name("Delete user")
                    .module("USER")
                    .build());

                //permission
                Permission createPermission = permissionRepository.save(Permission.builder()
                    .apiPath("/api/v1/permission")
                    .method(ApiMethod.POST)
                    .name("Create permission")
                    .module("PERMISSION")
                    .build());

                Permission getPermission = permissionRepository.save(Permission.builder()
                    .apiPath("/api/v1/permission")
                    .method(ApiMethod.GET)
                    .name("Fetch permission paginate")
                    .module("PERMISSION")
                    .build());

                Permission updatePermission = permissionRepository.save(Permission.builder()
                    .apiPath("/api/v1/permission/:id")
                    .method(ApiMethod.PATCH)
                    .name("Update permission")
                    .module("PERMISSION")
                    .build());
                
                Permission deletePermission = permissionRepository.save(Permission.builder()
                    .apiPath("/api/v1/permission/:id")
                    .method(ApiMethod.DELETE)
                    .name("Delete permission")
                    .module("PERMISSION")
                    .build());

                //role
                Permission getRole = permissionRepository.save(Permission.builder()
                    .apiPath("/api/v1/role")
                    .method(ApiMethod.GET)
                    .name("Fetch role paginate")
                    .module("ROLE")
                    .build());

                Permission getSingleRole = permissionRepository.save(Permission.builder()
                    .apiPath("/api/v1/role/:id")
                    .method(ApiMethod.GET)
                    .name("Fetch role by id")
                    .module("ROLE")
                    .build());

                Permission createRole = permissionRepository.save(Permission.builder()
                    .apiPath("/api/v1/role")
                    .method(ApiMethod.POST)
                    .name("Create role")
                    .module("ROLE")
                    .build());

                Permission updateRole = permissionRepository.save(Permission.builder()
                    .apiPath("/api/v1/role/:id")
                    .method(ApiMethod.PATCH)
                    .name("Update role")
                    .module("ROLE")
                    .build());

                Permission deleteRole = permissionRepository.save(Permission.builder()
                    .apiPath("/api/v1/role/:id")
                    .method(ApiMethod.DELETE)
                    .name("Delete role")
                    .module("ROLE")
                    .build());

                Set<Permission> permissions = new HashSet<Permission>();
                permissions = Stream.of(
                    getUser,
                    createUser,
                    updateUser,
                    deleteUser,
                    createPermission,
                    getPermission,
                    updatePermission,
                    deletePermission,
                    getRole,
                    createRole,
                    getSingleRole,
                    updateRole,
                    deleteRole
                ).collect(Collectors.toSet());
                
                //init role
                Role testerRole = roleRepository.save(Role.builder()
                    .name("TESTER")
                    .description("Tester role")
                    .isActive(true)
                    .build());

                Role userRole = roleRepository.save(Role.builder()
                    .name("USER")
                    .description("User role")
                    .isActive(true)
                    .build());

                Role adminRole = roleRepository.save(Role.builder()
                    .name("ADMIN")
                    .description("Admin role")
                    .isActive(true)
                    .permission(permissions)
                    .build());

                //init user
                userRepository.save(User.builder()
                    .username("Admin")
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .isActive(true)
                    .image("default.png")
                    .createdBy("system")
                    .accountType(AccountType.LOCAL)
                    .role(adminRole)
                    .build());

                userRepository.save(User.builder()
                    .username("User")
                    .email("user@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .isActive(true)
                    .image("image.jpg")
                    .createdBy("system")
                    .accountType(AccountType.FACEBOOK)
                    .role(userRole)
                    .build());

                userRepository.save(User.builder()
                    .username("Tester")
                    .email("test@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .isActive(true)
                    .image("test.svg")
                    .createdBy("system")
                    .accountType(AccountType.GOOGLE)
                    .role(testerRole)
                    .build());
            }
        };
    }
}
