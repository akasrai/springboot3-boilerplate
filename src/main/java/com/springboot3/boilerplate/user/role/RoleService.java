package com.springboot3.boilerplate.user.role;

import com.springboot3.boilerplate.app.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public Role findByName(RoleType role) {
        return roleRepository.findByName(role).orElseThrow(() -> new ResourceNotFoundException("Role", "name", role));
    }

    public boolean existsByName(RoleType role) {
        return roleRepository.existsByName(role);
    }
}
