package com.prabhakar.auth.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.prabhakar.auth.cache.CachedUserAuth;
import com.prabhakar.auth.cache.UserAuthCacheService;
import com.prabhakar.auth.model.Permission;
import com.prabhakar.auth.model.Role;
import com.prabhakar.auth.model.User;
import com.prabhakar.auth.repository.UserRepository;

//@Service
//public class MyUserDetailsService implements UserDetailsService {
//
//    private final UserRepository repo;
//
//    public MyUserDetailsService(UserRepository repo) {
//        this.repo = repo;
//    }
//    @Cacheable(value = "users", key = "#username")
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//
//        User user = repo.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        Set<GrantedAuthority> authorities = new HashSet<>();
//
//        // 1️⃣ Add role: ROLE_USER or ROLE_ADMIN
//        Role role = user.getRole();
//        authorities.add(new SimpleGrantedAuthority(role.getName()));
//
//        // 2️⃣ Add permissions from role
//        for (Permission p : role.getPermissions()) {
//            authorities.add(new SimpleGrantedAuthority(p.getName())); // e.g. USER_READ
//        }
//
//        return new org.springframework.security.core.userdetails.User(
//                user.getUsername(),
//                user.getPassword(),
//                authorities
//        );
//    }
//}

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository repo;
    private final UserAuthCacheService cacheService;

    public MyUserDetailsService(UserRepository repo, UserAuthCacheService cacheService) {
        this.repo = repo;
        this.cacheService = cacheService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1️⃣ Try to load from Redis cache
        CachedUserAuth cached = cacheService.getUserAuth(username);

        if (cached != null) {
            Set<GrantedAuthority> authorities = new HashSet<>();
            cached.getAuthorities().forEach(auth -> authorities.add(new SimpleGrantedAuthority(auth)));

            return new org.springframework.security.core.userdetails.User(
                    cached.getUsername(),
                    "N/A", // password not stored in cache
                    authorities
            );
        }

        // 2️⃣ If not cached → load from DB
        User user = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();
        Set<String> authorities = new HashSet<>();

        Role role = user.getRole();
        roles.add(role.getName());
        authorities.add(role.getName());

        for (Permission p : role.getPermissions()) {
            permissions.add(p.getName());
            authorities.add(p.getName());
        }

        // 3️⃣ Build Cached object
        CachedUserAuth dto = new CachedUserAuth();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRoles(new ArrayList<>(roles));
        dto.setPermissions(new ArrayList<>(permissions));
        dto.setAuthorities(new ArrayList<>(authorities));

        // 4️⃣ Save to Redis for 6 hours
        cacheService.saveUserAuth(username, dto);

        // 5️⃣ Return UserDetails
        Set<GrantedAuthority> grantedAuth = new HashSet<>();
        authorities.forEach(a -> grantedAuth.add(new SimpleGrantedAuthority(a)));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                grantedAuth
        );
    }
}
