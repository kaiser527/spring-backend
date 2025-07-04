package com.kaiser.spring_backend.services;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.kaiser.spring_backend.dto.reponse.AuthResponse;
import com.kaiser.spring_backend.dto.reponse.IntrospectResponse;
import com.kaiser.spring_backend.dto.reponse.UserResponse;
import com.kaiser.spring_backend.dto.request.AuthRequest;
import com.kaiser.spring_backend.dto.request.CreateUserRequest;
import com.kaiser.spring_backend.dto.request.IntrospectRequest;
import com.kaiser.spring_backend.entities.BlacklistToken;
import com.kaiser.spring_backend.entities.Role;
import com.kaiser.spring_backend.entities.User;
import com.kaiser.spring_backend.exception.AppException;
import com.kaiser.spring_backend.exception.ErrorCode;
import com.kaiser.spring_backend.mapper.UserMapper;
import com.kaiser.spring_backend.repositories.BlacklistTokenRepository;
import com.kaiser.spring_backend.repositories.RoleRepository;
import com.kaiser.spring_backend.repositories.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class AuthService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    BlacklistTokenRepository blacklistTokenRepository;
    UserMapper userMapper;
    RoleRepository roleRepository;
    
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @NonFinal
    @Value("${role.user}")
    protected String USER_ROLE;

    public IntrospectResponse introspect (IntrospectRequest request) throws JOSEException, ParseException {
        String token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }
        return IntrospectResponse.builder()
            .valid(isValid)
            .build();
    }

    public UserResponse getAccount(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));     
        
        return userMapper.toUserResponse(user);
    }

    public AuthResponse login(AuthRequest request){
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if(!authenticated || user.getIsActive() == false) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String token = generateToken(user);

        return AuthResponse.builder()
            .token(token)
            .isAuthenticated(authenticated)
            .user(userMapper.toUserResponse(user))
            .build();
    }

    public UserResponse register(CreateUserRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        Role role = roleRepository.findById(USER_ROLE).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));
        
        User user = userMapper.toCreateUser(request);

        user.setRole(role);

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    public void logout(String token) throws JOSEException, ParseException {
        try {
            SignedJWT signToken = verifyToken(token, true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
    
            BlacklistToken blacklistToken = BlacklistToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();
    
            blacklistTokenRepository.save(blacklistToken);
        } catch (AppException e) {
            log.info("Token is already expired");
        }
    }

    public AuthResponse refreshToken(String token) throws JOSEException, ParseException {
        SignedJWT signToken = verifyToken(token, true);

        String jit = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
    
        BlacklistToken blacklistToken = BlacklistToken.builder()
            .id(jit)
            .expiryTime(expiryTime)
            .build();
    
        blacklistTokenRepository.save(blacklistToken);

        String email = signToken.getJWTClaimsSet().getSubject();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        String access_token = generateToken(user);

        return AuthResponse.builder()
            .token(access_token)
            .isAuthenticated(true)
            .user(userMapper.toUserResponse(user))
            .build();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier jwsVerifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expireTime = isRefresh 
            ? new Date(
                signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli()
            ) 
            : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(jwsVerifier);

        if(!(verified && expireTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if(blacklistTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    private String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
            .subject(user.getEmail())
            .issuer("kaiser.com")
            .issueTime(new Date())
            .expirationTime(new Date(
                Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
            ))
            .jwtID(UUID.randomUUID().toString())
            .claim("role", user.getRole().getName())
            .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        
        try{
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        }catch(JOSEException e){
            throw new RuntimeException(e);
        }
    }
}
