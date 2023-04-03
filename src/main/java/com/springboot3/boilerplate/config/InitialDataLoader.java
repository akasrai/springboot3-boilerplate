package com.springboot3.boilerplate.config;

import com.springboot3.boilerplate.app.enums.AuthProvider;
import com.springboot3.boilerplate.user.role.RoleType;
import com.springboot3.boilerplate.user.admin.Admin;
import com.springboot3.boilerplate.user.admin.AdminRepository;
import com.springboot3.boilerplate.user.role.Role;
import com.springboot3.boilerplate.user.role.RoleRepository;
import com.springboot3.boilerplate.user.role.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;

@Component
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private AdminCredentialsConfig adminCredentialsConfig;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup)
            return;


        createRoleIfNotFound(RoleType.ROLE_ADMIN);
        createRoleIfNotFound(RoleType.ROLE_USER);
        createRoleIfNotFound(RoleType.ROLE_GUEST);

        Role adminRole = roleService.findByName(RoleType.ROLE_ADMIN);
        createAdminIfNotFound(adminRole);

        alreadySetup = true;
    }

    @Transactional
    public Admin createAdminIfNotFound(Role adminRole) {
        Admin admin = new Admin();

        if (!adminRepository.existsByEmail(adminCredentialsConfig.getEmail())) {
            admin.setEmail(adminCredentialsConfig.getEmail());
            admin.setFirstName(adminCredentialsConfig.getFirstName());
            admin.setLastName(adminCredentialsConfig.getLastName());
            admin.setProvider(AuthProvider.SYSTEM);
            admin.setPassword(passwordEncoder.encode(adminCredentialsConfig.getPassword()));
            admin.setRoles(new ArrayList<>(Collections.singletonList(adminRole)));
            adminRepository.save(admin);
        }

        return admin;
    }

    @Transactional
    public void createRoleIfNotFound(RoleType name) {
        if (!roleService.existsByName(name)) {
            Role role = new Role(name);

            roleRepository.save(role);
        }
    }
}