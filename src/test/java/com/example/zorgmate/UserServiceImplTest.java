package com.example.zorgmate;

import com.example.zorgmate.dal.entity.User.User;
import com.example.zorgmate.dal.entity.User.UserRole;
import com.example.zorgmate.dal.repository.UserRepository;
import com.example.zorgmate.dto.User.RegisterRequest;
import com.example.zorgmate.dto.User.UpdateUserRequest;
import com.example.zorgmate.dto.User.UserDTO;
import com.example.zorgmate.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserServiceImpl userService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_shouldCreateUserWithDefaultRoleUser() {
        // Arrange
        RegisterRequest request = new RegisterRequest("john", "secret", null);

        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encodedPass");

        when(userRepository.save(any())).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        // Act
        UserDTO result = userService.registerUser(request);

        // Assert
        assertNotNull(result);
        assertEquals("john", result.getUsername());
        assertEquals(UserRole.USER, result.getRole());
    }

    @Test
    void registerUser_shouldCreateUserWithCustomRole() {
        // Arrange
        RegisterRequest request = new RegisterRequest("admin", "secure", UserRole.ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secure")).thenReturn("enc");

        when(userRepository.save(any())).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(2L);
            return u;
        });

        // Act
        UserDTO result = userService.registerUser(request);

        // Assert
        assertEquals("admin", result.getUsername());
        assertEquals(UserRole.ADMIN, result.getRole());
    }

    @Test
    void registerUser_shouldThrowExceptionWhenUsernameExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest("john", "pw", null);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(new User()));

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
    }

    @Test
    void getAllUsers_shouldReturnListOfUserDTOs() {
        // Arrange
        User user = User.builder().id(1L).username("john").role(UserRole.USER).build();
        when(userRepository.findAll()).thenReturn(List.of(user));

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertEquals(1, result.size());
        assertEquals("john", result.get(0).getUsername());
    }

    @Test
    void getUserById_shouldReturnUserDTOIfExists() {
        // Arrange
        User user = User.builder().id(5L).username("jane").role(UserRole.ADMIN).build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        // Act
        UserDTO result = userService.getUserById(5L);

        // Assert
        assertEquals("jane", result.getUsername());
        assertEquals(UserRole.ADMIN, result.getRole());
    }

    @Test
    void getUserById_shouldThrowWhenNotFound() {
        // Arrange
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(404L));
    }

    @Test
    void updateUser_shouldModifyUsernameAndRole() {
        // Arrange
        User user = User.builder().id(1L).username("old").role(UserRole.USER).build();
        UpdateUserRequest update = new UpdateUserRequest("new", UserRole.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDTO result = userService.updateUser(1L, update);

        // Assert
        assertEquals("new", result.getUsername());
        assertEquals(UserRole.ADMIN, result.getRole());
    }

    @Test
    void updateUser_shouldThrowWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.empty());
        UpdateUserRequest update = new UpdateUserRequest("test", UserRole.USER);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(100L, update));
    }

    @Test
    void deleteUser_shouldCallDeleteWhenUserExists() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_shouldThrowWhenUserNotFound() {
        // Arrange
        when(userRepository.existsById(99L)).thenReturn(false);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(99L));
    }

    @Test
    void getUserByUsername_shouldReturnDTOIfExists() {
        // Arrange
        User user = User.builder().id(3L).username("tester").role(UserRole.USER).build();
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));

        // Act
        UserDTO result = userService.getUserByUsername("tester");

        // Assert
        assertEquals("tester", result.getUsername());
        assertEquals(UserRole.USER, result.getRole());
    }

    @Test
    void getUserByUsername_shouldThrowIfNotFound() {
        // Arrange
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByUsername("missing"));
    }
}
