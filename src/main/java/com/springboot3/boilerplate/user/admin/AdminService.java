package com.springboot3.boilerplate.user.admin;

import com.springboot3.boilerplate.user.role.RoleType;
import com.springboot3.boilerplate.app.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminMapper adminMapper;

    public Admin findByEmail(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("admin", "id", email));
    }

    public AdminResponse getCurrentAdmin(String email) {
        Admin admin = findByEmail(email);

        AdminResponse adminResponse = adminMapper.toAdminResponse(admin);
        adminResponse.setRoles(admin.getRoles().stream()
                .map(role -> RoleType.valueOf(role.getName().toString()).toString().split("_")[1])
                .toList());

        return adminResponse;
    }
}