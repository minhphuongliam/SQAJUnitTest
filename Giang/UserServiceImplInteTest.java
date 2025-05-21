package com.thanhtam.backend;

import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.PasswordResetTokenRepository;
import com.thanhtam.backend.repository.RoleRepository;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.service.UserService;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
public class UserServiceImplInteTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String generateUniqueUsername(String base) {
        return base + "_" + UUID.randomUUID().toString().substring(0, 6);
    }

    @Test
    void _11_14_createUser_rolesNull_defaultStudent() {
        String username = generateUniqueUsername("stu");

        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("stu");
        user.setRoles(null);

        User saved = userService.createUser(user);
        assertNotNull(saved.getId());
        assertTrue(saved.getRoles().stream().anyMatch(r -> r.getName() == ERole.ROLE_STUDENT));
    }

    @Test
    void _11_15_createUser_adminProvided_allRolesAdded() {
        String username = generateUniqueUsername("admin");

        Role admin = roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow();

        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("adminuser");
        user.setRoles(Set.of(admin));

        User saved = userService.createUser(user);
        assertNotNull(saved.getId());
        assertEquals(3, saved.getRoles().size());
    }

    @Test
    void _11_20_updateUser() {
        String username = generateUniqueUsername("update");

        // Step 1: Create & save the original user
        User user = new User();
        user.setUsername(username);
        user.setEmail("old@example.com");
        user.setPassword("pw");
        User saved = userService.createUser(user);

        // Step 2: Update only the email
        saved.setEmail("new@example.com");
        userService.updateUser(saved);

        // Step 3: Verify
        Optional<User> result = userRepository.findByUsername(username);
        assertTrue(result.isPresent());
        assertEquals("new@example.com", result.get().getEmail());
    }


    @Test
    void _11_27_resetPassword_success() {
        String username = generateUniqueUsername("reset");

        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("oldPw");

        User saved = userService.createUser(user);

        // Simulate password reset
        saved.setPassword(passwordEncoder.encode("newPw"));
        userRepository.save(saved);

        Optional<User> result = userRepository.findByUsername(username);
        assertTrue(result.isPresent());
        assertTrue(passwordEncoder.matches("newPw", result.get().getPassword()));
    }

    // @Test
    // void rollbackCheck_test() {
    //     String username = "check_" + UUID.randomUUID().toString().substring(0, 5);
    //     User user = new User();
    //     user.setUsername(username);
    //     user.setEmail(username + "@test.com");
    //     user.setPassword("test");
    //     userService.createUser(user);

    //     assertTrue(userRepository.findByUsername(username).isPresent());
    // }
}
