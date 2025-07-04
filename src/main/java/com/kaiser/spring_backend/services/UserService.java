package com.kaiser.spring_backend.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.kaiser.spring_backend.dto.reponse.PaginatedResponse;
import com.kaiser.spring_backend.dto.reponse.UserResponse;
import com.kaiser.spring_backend.dto.request.CreateUserRequest;
import com.kaiser.spring_backend.dto.request.UpdateUserRequest;
import com.kaiser.spring_backend.entities.Role;
import com.kaiser.spring_backend.entities.User;
import com.kaiser.spring_backend.exception.AppException;
import com.kaiser.spring_backend.exception.ErrorCode;
import com.kaiser.spring_backend.mapper.UserMapper;
import com.kaiser.spring_backend.repositories.RoleRepository;
import com.kaiser.spring_backend.repositories.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository repository;

    @NonFinal
    @Value("${role.admin}")
    protected String ADMIN_ROLE;

    public UserResponse createUser(CreateUserRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        Role role = repository.findById(request.getRoleId()).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));
        User user = userMapper.toCreateUser(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setCreatedBy(authentication.getName());
        user.setRole(role);

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    public PaginatedResponse<UserResponse> getUserPaginated(Pageable pageable){
        Page<User> userPage = userRepository.findAll(pageable);

        List<UserResponse> userResponses = userPage.getContent()
            .stream()
            .map(userMapper::toUserResponse)
            .toList();

        return PaginatedResponse.<UserResponse>builder()
            .pageNumber(userPage.getNumber() + 1)
            .pageSize(userPage.getSize())
            .totalPages(userPage.getTotalPages())
            .data(userResponses)
            .build();
    }

    public UserResponse updateUser(String id, UpdateUserRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Role role = repository.findById(request.getRoleId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        userMapper.toUpdateUser(user, request);
        
        user.setUpdatedBy(authentication.getName());
        user.setRole(role);

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    public UserResponse deleteUser(String id){
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        if(user.getRole().getId().equals(ADMIN_ROLE)){
            throw new AppException(ErrorCode.DELETE_ADMIN_USER);
        }

        userRepository.delete(user);

        return userMapper.toUserResponse(user);
    }
}
