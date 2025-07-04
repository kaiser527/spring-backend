package com.kaiser.spring_backend.services;

import java.util.HashSet;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.kaiser.spring_backend.dto.reponse.PaginatedResponse;
import com.kaiser.spring_backend.dto.reponse.RoleResponse;
import com.kaiser.spring_backend.dto.request.RoleRequest;
import com.kaiser.spring_backend.entities.Permission;
import com.kaiser.spring_backend.entities.Role;
import com.kaiser.spring_backend.exception.AppException;
import com.kaiser.spring_backend.exception.ErrorCode;
import com.kaiser.spring_backend.mapper.RoleMapper;
import com.kaiser.spring_backend.repositories.PermissionRepository;
import com.kaiser.spring_backend.repositories.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    @NonFinal
    @Value("${role.admin}")
    protected String ADMIN_ROLE;

    public RoleResponse createRole(RoleRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(roleRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.ROLE_EXIST);
        }

        List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());
        if(permissions.size() == 0){
            throw new AppException(ErrorCode.PERMISSION_NOT_EXIST);
        }

        Role role = roleMapper.toCreateRole(request);
        
        role.setPermission(new HashSet<Permission>(permissions));
        role.setCreatedBy(authentication.getName());

        roleRepository.save(role);

        return roleMapper.toRoleResponse(role);
    }

    public RoleResponse updateRole(String id, RoleRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Role role = roleRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));

        if(roleRepository.existsByNameAndIdNot(id, request.getName())){
            throw new AppException(ErrorCode.ROLE_EXIST);
        }

        List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());
        if(permissions.size() == 0){
            throw new AppException(ErrorCode.PERMISSION_NOT_EXIST);
        }

        roleMapper.toUpdateRole(role, request);

        role.setPermission(new HashSet<Permission>(permissions));
        role.setUpdatedBy(authentication.getName());

        roleRepository.save(role);

        return roleMapper.toRoleResponse(role);
    }
    
    public PaginatedResponse<RoleResponse> getRolePaginated(Pageable page){
        Page<Role> rolePage = roleRepository.findAll(page);

        List<RoleResponse> roleResponses = rolePage.getContent()
            .stream()
            .map(roleMapper::toRoleResponse)
            .toList();

        return PaginatedResponse.<RoleResponse>builder()
            .pageNumber(rolePage.getNumber() + 1)
            .pageSize(rolePage.getSize())
            .totalPages(rolePage.getTotalPages())
            .data(roleResponses)
            .build();
    }

    public RoleResponse getSingleRole(String id){
        Role role = roleRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));

        return roleMapper.toRoleResponse(role);
    }

    public RoleResponse deleteRole(String id){
        Role role = roleRepository.findWithPermissionsById(id).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));
        
        if(role.getId().equals(ADMIN_ROLE)){
            throw new AppException(ErrorCode.DELETE_ADMIN_ROLE);
        }

        roleRepository.delete(role);

        return roleMapper.toRoleResponse(role);
    }
}
