package com.prabhakar.auth.service;

import com.prabhakar.auth.model.Permission;
import com.prabhakar.auth.model.Role;
import com.prabhakar.auth.model.User;
import com.prabhakar.auth.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    public MyUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1️⃣ Add role: ROLE_USER or ROLE_ADMIN
        Role role = user.getRole();
        authorities.add(new SimpleGrantedAuthority(role.getName()));

        // 2️⃣ Add permissions from role
        for (Permission p : role.getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(p.getName())); // e.g. USER_READ
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
