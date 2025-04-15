package com.thanhtam.backend;

import com.thanhtam.backend.service.EmailService;
import com.thanhtam.backend.service.RoleService;
import com.thanhtam.backend.service.UserServiceImpl;
import com.thanhtam.backend.dto.UserExport;
import com.thanhtam.backend.entity.PasswordResetToken;
import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.PasswordResetTokenRepository;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.mail.MessagingException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private UserRepository userRepository;
    private RoleService roleService;
    private PasswordEncoder passwordEncoder;
    private PasswordResetTokenRepository passwordResetTokenRepository;
    private EmailService emailService;
    private UserServiceImpl service;

    private Role roleAdmin;
    private Role roleLecturer;
    private Role roleStudent;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleService = mock(RoleService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
        emailService = mock(EmailService.class);

        service = new UserServiceImpl(userRepository, roleService, passwordEncoder,
                passwordResetTokenRepository, emailService);

        roleAdmin = new Role();
        roleAdmin.setName(ERole.ROLE_ADMIN);
        roleLecturer = new Role();
        roleLecturer.setName(ERole.ROLE_LECTURER);
        roleStudent = new Role();
        roleStudent.setName(ERole.ROLE_STUDENT);
    }

    // ---------- Helper ----------
    private User makeUser(Long id, String username, String email) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("pw");
        Profile p = new Profile();
        p.setFirstName("First");
        p.setLastName("Last");
        u.setProfile(p);
        return u;
    }

    // 9.1 Get user by username (found)
    @Test
    void _9_1_getUserByUsername_found() {
        User u = makeUser(1L, "john", "j@e.com");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(u));

        Optional<User> result = service.getUserByUsername("john");

        assertTrue(result.isPresent());
        assertEquals("john", result.get().getUsername());
    }

    // 9.2 Get user by username (not found)
    @Test
    void _9_2_getUserByUsername_notFound() {
        when(userRepository.findByUsername("none")).thenReturn(Optional.empty());
        assertFalse(service.getUserByUsername("none").isPresent());
    }

    // 9.3 Get currently authenticated username
    @Test
    void _9_3_getCurrentUsername() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("alice");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            mocked.when(SecurityContextHolder::getContext).thenReturn(ctx);
            assertEquals("alice", service.getUserName());
        }
    }

    // 9.4 Check existence by username (exists)
    @Test
    void _9_4_existsByUsername_exists() {
        when(userRepository.existsByUsername("bob")).thenReturn(true);
        assertTrue(service.existsByUsername("bob"));
    }

    // 9.5 Check existence by username (not exists)
    @Test
    void _9_5_existsByUsername_notExists() {
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        assertFalse(service.existsByUsername("bob"));
    }

    // 9.6 Check existence by email (exists)
    @Test
    void _9_6_existsByEmail_exists() {
        when(userRepository.existsByEmail("e@e.com")).thenReturn(true);
        assertTrue(service.existsByEmail("e@e.com"));
    }

    // 9.7 Check existence by email (not exists)
    @Test
    void _9_7_existsByEmail_notExists() {
        when(userRepository.existsByEmail("e@e.com")).thenReturn(false);
        assertFalse(service.existsByEmail("e@e.com"));
    }

    // 9.8 Paginated user list (non-empty)
    @Test
    void _9_8_findUsersByPage_nonEmpty() {
        List<User> users = List.of(makeUser(1L, "u1", "u1@e.com"));
        Page<User> page = new PageImpl<>(users);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
        assertFalse(service.findUsersByPage(Pageable.unpaged()).isEmpty());
    }

    // 9.9 Paginated user list (empty)
    @Test
    void _9_9_findUsersByPage_empty() {
        when(userRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
        assertTrue(service.findUsersByPage(Pageable.unpaged()).isEmpty());
    }

    // 9.10 Paginated deleted users (non-empty)
    @Test
    void _9_10_findDeletedUsers_nonEmpty() {
        when(userRepository.findAllByDeleted(true, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(makeUser(2L, "del", "d@e.com"))));
        assertFalse(service.findUsersDeletedByPage(Pageable.unpaged(), true).isEmpty());
    }

    // 9.11 Paginated deleted users (empty)
    @Test
    void _9_11_findDeletedUsers_empty() {
        when(userRepository.findAllByDeleted(false, Pageable.unpaged())).thenReturn(Page.empty());
        assertTrue(service.findUsersDeletedByPage(Pageable.unpaged(), false).isEmpty());
    }

    // 9.12 Paginated users by username contains (non-empty)
    @Test
    void _9_12_findByUsernameContains_nonEmpty() {
        when(userRepository.findAllByDeletedAndUsernameContains(false, "jo", Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(makeUser(1L, "john", "j@e.com"))));
        assertFalse(service.findAllByDeletedAndUsernameContains(false, "jo", Pageable.unpaged()).isEmpty());
    }

    // 9.13 Paginated users by username contains (empty)
    @Test
    void _9_13_findByUsernameContains_empty() {
        when(userRepository.findAllByDeletedAndUsernameContains(true, "x", Pageable.unpaged()))
                .thenReturn(Page.empty());
        assertTrue(service.findAllByDeletedAndUsernameContains(true, "x", Pageable.unpaged()).isEmpty());
    }

    // 9.14 Create user – roles == null (default ROLE_STUDENT)
    @Test
    void _9_14_createUser_rolesNull_defaultStudent() {
        User input = new User();
        input.setUsername("stu");
        input.setEmail("s@e.com");
        input.setRoles(null);

        when(passwordEncoder.encode("stu")).thenReturn("encStu");
        when(roleService.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(roleStudent));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = service.createUser(input);
        assertTrue(saved.getRoles().contains(roleStudent));
    }

    // 9.15 Create user – ROLE_ADMIN provided (should add ADMIN, LECTURER, STUDENT)
    @Test
    void _9_15_createUser_adminProvided_allRolesAdded() {
        User input = new User();
        input.setUsername("adm");
        input.setEmail("a@e.com");
        input.setRoles(Set.of(roleAdmin));

        when(passwordEncoder.encode("adm")).thenReturn("encAdm");
        when(roleService.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(roleAdmin));
        when(roleService.findByName(ERole.ROLE_LECTURER)).thenReturn(Optional.of(roleLecturer));
        when(roleService.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(roleStudent));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = service.createUser(input);
        assertEquals(3, saved.getRoles().size());
        assertTrue(saved.getRoles().contains(roleAdmin));
    }

    // 9.16 Find user by ID (found)
    @Test
    void _9_16_findUserById_found() {
        User u = makeUser(5L, "u5", "u5@e.com");
        when(userRepository.findById(5L)).thenReturn(Optional.of(u));
        assertTrue(service.findUserById(5L).isPresent());
    }

    // 9.17 Find user by ID (not found)
    @Test
    void _9_17_findUserById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertFalse(service.findUserById(99L).isPresent());
    }

    // 9.18 Export non-deleted users
    @Test
    void _9_18_exportNonDeletedUsers() {
        User u = spy(makeUser(1L, "john", "j@e.com"));
        when(userRepository.findAllByDeleted(false)).thenReturn(List.of(u));

        List<UserExport> exports = service.findAllByDeletedToExport(false);
        assertEquals(1, exports.size());
        assertEquals("john", exports.get(0).getUsername());
    }

    // 9.19 Export users – no users found
    @Test
    void _9_19_exportNoUsers() {
        when(userRepository.findAllByDeleted(true)).thenReturn(Collections.emptyList());
        assertTrue(service.findAllByDeletedToExport(true).isEmpty());
    }

    // 9.20 Update user
    @Test
    void _9_20_updateUser() {
        User u = makeUser(3L, "u3", "u3@e.com");
        service.updateUser(u);
        verify(userRepository).save(u);
    }

    // 9.21 Find all users by intakeId (non-empty)
    @Test
    void _9_21_findAllByIntakeId_nonEmpty() {
        when(userRepository.findAllByIntakeId(1L)).thenReturn(List.of(makeUser(1L, "u", "u@e.com")));
        assertFalse(service.findAllByIntakeId(1L).isEmpty());
    }

    // 9.22 Find all users by intakeId (empty)
    @Test
    void _9_22_findAllByIntakeId_empty() {
        when(userRepository.findAllByIntakeId(2L)).thenReturn(Collections.emptyList());
        assertTrue(service.findAllByIntakeId(2L).isEmpty());
    }

    // 9.23 Request password reset – user not found
    @Test
    void _9_23_requestPasswordReset_userNotFound() throws MessagingException {
        when(userRepository.findByEmail("x@e.com")).thenReturn(Optional.empty());
        assertFalse(service.requestPasswordReset("x@e.com"));
        verify(passwordResetTokenRepository, never()).save(any());
    }

    // 9.24 Request password reset – user found
    @Test
    void _9_24_requestPasswordReset_userFound() throws MessagingException {
        User u = makeUser(1L, "john", "j@e.com");
        when(userRepository.findByEmail("j@e.com")).thenReturn(Optional.of(u));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

        try (MockedConstruction<com.thanhtam.backend.config.JwtUtils> mocked =
                     Mockito.mockConstruction(com.thanhtam.backend.config.JwtUtils.class, (mock, ctx) ->
                             when(mock.generatePasswordResetToken(1L)).thenReturn("token123"))) {

            assertTrue(service.requestPasswordReset("j@e.com"));
            verify(emailService).resetPassword("j@e.com", "token123");
        }
    }

    // 9.25 Reset password – token expired
    @Test
    void _9_25_resetPassword_tokenExpired() {
        try (MockedConstruction<com.thanhtam.backend.config.JwtUtils> mocked =
                     Mockito.mockConstruction(com.thanhtam.backend.config.JwtUtils.class, (mock, ctx) ->
                             when(mock.hasTokenExpired("bad")).thenReturn(true))) {
            assertFalse(service.resetPassword("bad", "new"));
        }
    }

    // 9.26 Reset password – token not expired, but PasswordResetToken null
    @Test
    void _9_26_resetPassword_tokenNotFound() {
        when(passwordResetTokenRepository.findByToken("tok")).thenReturn(null);
        try (MockedConstruction<com.thanhtam.backend.config.JwtUtils> mocked =
                     Mockito.mockConstruction(com.thanhtam.backend.config.JwtUtils.class, (mock, ctx) ->
                             when(mock.hasTokenExpired("tok")).thenReturn(false))) {
            assertFalse(service.resetPassword("tok", "pw"));
        }
    }

    // 9.27 Reset password – success
    @Test
    void _9_27_resetPassword_success() {
        User u = makeUser(1L, "john", "j@e.com");
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken("good");
        prt.setUser(u);

        when(passwordResetTokenRepository.findByToken("good")).thenReturn(prt);
        when(passwordEncoder.encode("newPw")).thenReturn("encNewPw");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        try (MockedConstruction<com.thanhtam.backend.config.JwtUtils> mocked =
                     Mockito.mockConstruction(com.thanhtam.backend.config.JwtUtils.class, (mock, ctx) ->
                             when(mock.hasTokenExpired("good")).thenReturn(false))) {
            assertTrue(service.resetPassword("good", "newPw"));
            verify(passwordResetTokenRepository).delete(prt);
        }
    }

    // 9.28 Paginated search by username or email (non-empty)
    @Test
    void _9_28_searchUsernameOrEmail_nonEmpty() {
        when(userRepository.findAllByUsernameContainsOrEmailContains("a", "a", Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(makeUser(1L, "amy", "a@e.com"))));
        assertFalse(service.findAllByUsernameContainsOrEmailContains("a", "a", Pageable.unpaged()).isEmpty());
    }

    // 9.29 Paginated search by username or email (empty)
    @Test
    void _9_29_searchUsernameOrEmail_empty() {
        when(userRepository.findAllByUsernameContainsOrEmailContains("x", "x", Pageable.unpaged()))
                .thenReturn(Page.empty());
        assertTrue(service.findAllByUsernameContainsOrEmailContains("x", "x", Pageable.unpaged()).isEmpty());
    }

    // 9.30 addRoles() – role found
    @Test
    void _9_30_addRoles_roleFound() {
        Set<Role> roles = new HashSet<>();
        when(roleService.findByName(ERole.ROLE_LECTURER)).thenReturn(Optional.of(roleLecturer));
        service.addRoles(ERole.ROLE_LECTURER, roles);
        assertTrue(roles.contains(roleLecturer));
    }

    // 9.31 addRoles() – role not found
    @Test
    void _9_31_addRoles_roleNotFound() {
        when(roleService.findByName(ERole.ROLE_LECTURER)).thenReturn(Optional.empty());
        Set<Role> roles = new HashSet<>();
        assertThrows(RuntimeException.class, () -> service.addRoles(ERole.ROLE_LECTURER, roles));
    }
}

