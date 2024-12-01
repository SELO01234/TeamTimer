package com.example.application.auth.service;

import com.example.application.auth.dto.AuthenticationRequest;
import com.example.application.auth.dto.AuthenticationResponse;
import com.example.application.auth.dto.RefreshTokenRequest;
import com.example.application.auth.dto.RegisterRequest;
import com.example.application.security.service.JwtService;
import com.example.application.user.model.Role;
import com.example.application.user.model.User;
import com.example.application.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public String register(RegisterRequest registerRequest) {

        if(userRepository.existsByUsername(registerRequest.getUsername())){
            return "User has already been registered";
        }

        try{

            Role role;

            if(registerRequest.getRole() == null){
                role = Role.USER;
            }
            else{
                role = registerRequest.getRole();
            }

            User user = User.builder()
                    .email(registerRequest.getEmail())
                    .username(registerRequest.getUsername())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .timezone(registerRequest.getTimezone())
                    .role(role)
                    .build();

            userRepository.save(user);

            return "User is saved";
        }
        catch (Exception exception){
            return "Could not save user";
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );

        var user = userRepository.findByUsername(authenticationRequest.getUsername()).orElseThrow();

        String accessToken = jwtService.generateToken(user, "access");
        String refreshToken = jwtService.generateToken(user, "refresh");

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        final String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username is not valid"));

        if(!jwtService.isTokenValid(refreshToken, userDetails)){
            throw new RuntimeException("Token is not valid!");
        }

        String accessToken = jwtService.generateToken(userDetails, "access");
        String newRefreshToken = jwtService.generateToken(userDetails, "refresh");

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
