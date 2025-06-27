package com.kaiser.spring_backend.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.kaiser.spring_backend.dto.reponse.ApiResponse;
import com.kaiser.spring_backend.dto.reponse.PaginatedResponse;
import com.kaiser.spring_backend.dto.reponse.UserResponse;
import com.kaiser.spring_backend.dto.request.CreateUserRequest;
import com.kaiser.spring_backend.dto.request.UpdateUserRequest;
import com.kaiser.spring_backend.services.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request){
        return ApiResponse.<UserResponse>builder()
            .message("Create user")
            .result(userService.createUser(request))
            .build();
    }

    @GetMapping
    ApiResponse<PaginatedResponse<UserResponse>> getUserPaginated(@RequestParam int pageSize, @RequestParam int pageNumber){
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);

        return ApiResponse.<PaginatedResponse<UserResponse>>builder()
            .message("Fetch user paginate")
            .result(userService.getUserPaginated(pageable))
            .build();
    }

    @PatchMapping("/{id}")
    ApiResponse<UserResponse> updateUser(@PathVariable("id") String id, @RequestBody @Valid UpdateUserRequest request){
        return ApiResponse.<UserResponse>builder()
            .result(userService.updateUser(id, request))
            .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<UserResponse> deleteUser(@PathVariable("id") String id){
        return ApiResponse.<UserResponse>builder()
            .result(userService.deleteUser(id))
            .build();
    }
}
