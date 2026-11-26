package gal.usc.etse.es.tapeacompostela.service;

import gal.usc.etse.es.tapeacompostela.exception.NotFoundException;
import gal.usc.etse.es.tapeacompostela.model.dto.UserDTO;
import gal.usc.etse.es.tapeacompostela.model.entity.Role;
import gal.usc.etse.es.tapeacompostela.model.entity.User;
import gal.usc.etse.es.tapeacompostela.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

// UserDetailsService le dice a Spring Security cómo encontrar a un usuario en la
// base de datos. El AuthenticationManager llama a loadUserByUsername durante el
// login y compara la contraseña con el hash
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {
    private final UserRepository users;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // .roles("ADMIN") añade el prefijo por sí mismo: la autoridad es ROLE_ADMIN
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles().stream().map(Role::getName).toArray(String[]::new))
                .build();
    }

    public UserDTO getUser(String username) {
        return users.findByUsername(username).map(UserService::toDTO).orElseThrow(() -> new NotFoundException("User", username));
    }

    static UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPoints(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.getCreatedAt()
        );
    }
}