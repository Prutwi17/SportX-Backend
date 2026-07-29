package com.sportx.backend.config;

import com.sportx.backend.entity.Category;
import com.sportx.backend.entity.Role;
import com.sportx.backend.entity.User;
import com.sportx.backend.enums.RoleName;
import com.sportx.backend.repository.CategoryRepository;
import com.sportx.backend.repository.RoleRepository;
import com.sportx.backend.repository.UserRepository;
import com.sportx.backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRoles();
        seedCategories();
        seedAdminUser();
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() ->
                    roleRepository.save(Role.builder().name(roleName).build()));
        }
    }

    private void seedCategories() {
        seedCategory("Cricket", "Cricket equipment and gear");
        seedCategory("Football", "Football equipment and gear");
    }

    private void seedCategory(String name, String description) {
        if (categoryRepository.findBySlug(SlugUtil.toSlug(name)).isEmpty()) {
            categoryRepository.save(Category.builder()
                    .name(name)
                    .slug(SlugUtil.toSlug(name))
                    .description(description)
                    .active(true)
                    .build());
        }
    }

    private void seedAdminUser() {
        if (userRepository.existsByEmail("admin@sportx.com")) {
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow();

        userRepository.save(User.builder()
                .email("admin@sportx.com")
                .password(passwordEncoder.encode("Admin@123"))
                .firstName("SportX")
                .lastName("Admin")
                .roles(Set.of(adminRole))
                .enabled(true)
                .build());
    }
}
