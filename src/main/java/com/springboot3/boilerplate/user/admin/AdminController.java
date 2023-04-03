package com.springboot3.boilerplate.user.admin;

import com.springboot3.boilerplate.security.CurrentUser;
import com.springboot3.boilerplate.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminResponse getCurrentAdmin(@CurrentUser UserPrincipal userPrincipal) {
        return adminService.getCurrentAdmin(userPrincipal.getEmail());
    }
}
