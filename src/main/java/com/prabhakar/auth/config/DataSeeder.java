package com.prabhakar.auth.config;

import com.prabhakar.auth.model.Permission;
import com.prabhakar.auth.model.Role;
import com.prabhakar.auth.repository.PermissionRepository;
import com.prabhakar.auth.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataSeeder {

    private final PermissionRepository permissionRepo;
    private final RoleRepository roleRepo;

    public DataSeeder(PermissionRepository permissionRepo, RoleRepository roleRepo) {
        this.permissionRepo = permissionRepo;
        this.roleRepo = roleRepo;
    }

    @PostConstruct
    public void seed() {

        // 1️⃣ Seed Permissions
        Permission pRead = createPermission("USER_READ");
        Permission pWrite = createPermission("USER_WRITE");
        Permission pDelete = createPermission("USER_DELETE");

        // 2️⃣ Seed roles
        Role userRole = createRole("ROLE_USER", Set.of(pRead));
        Role adminRole = createRole("ROLE_ADMIN", Set.of(pRead, pWrite));
        Role superRole = createRole("ROLE_SUPERADMIN", Set.of(pRead, pWrite, pDelete));
    }

    private Permission createPermission(String name) {
        return permissionRepo.findByName(name)
                .orElseGet(() -> permissionRepo.save(new Permission(null, name)));
    }

    private Role createRole(String name, Set<Permission> permissions) {
        return roleRepo.findByName(name)
                .orElseGet(() -> roleRepo.save(new Role(null, name, permissions)));
    }
}
