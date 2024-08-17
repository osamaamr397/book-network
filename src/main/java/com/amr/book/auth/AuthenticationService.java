package com.amr.book.auth;

import com.amr.book.email.EmailService;
import com.amr.book.email.EmailTemplate;
import com.amr.book.role.RoleRepository;
import com.amr.book.security.JwtService;
import com.amr.book.user.Token;
import com.amr.book.user.TokenRepository;
import com.amr.book.user.User;
import com.amr.book.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    //after declaration of AuthenticationManager we will jump to BeansConfig
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    public void register(RegistrationRequest request) throws MessagingException {
        var userRole = roleRepository.findByName("USER")
                // todo - better exception handling
                .orElseThrow(() -> new IllegalStateException("ROLE USER was not initiated"));
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))//as because in BeanConfig ->our AuthenticationProvider use the password encoder
                //so when spring perform check will check if the password or role password provider matches
                //the Hash password so if doesn't match it will say the password is not okay and will not authenticated
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();
        userRepository.save(user);
        sendValidationEmail(user);

    }


    private void sendValidationEmail(User user) throws MessagingException {

        var newToken=generateAndSaveActivationToken(user);
        emailService.sendEmail(user.getEmail(), user.fullName(),
                EmailTemplate.ACTIVATE_ACCOUNT,activationUrl,newToken,"Account Activation");

    }

    private String generateAndSaveActivationToken(User user) {
        //generate token
        String generateToken=generateActivationCode(6);
        var token = Token.builder().token(generateToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);

        return generateToken;
    }

    //this method will generate an activation code compose of 6 digits
    private String generateActivationCode(int length) {
        String characters = "0123456789";//chars which will compose the token
        StringBuilder codeBuilder= new StringBuilder();
        //to generate a random value we don't just use a random class
        SecureRandom secureRandom= new SecureRandom();
        for (int i=0;i<length;i++){
            int randomIndex=secureRandom.nextInt(characters.length());
            //this mean that this random index will be from 0 to 9
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        //this method will take care of whole authentication process if the username and password are correct
        //other wise it will throw an exception
        //will use the bean which configured in the BeansConfig
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )

        );
        var claims = new HashMap<String,Object>();
        /*
        so i will need my user so i don't need to fetch it again from the database
        because i can get it from authentication as this authenticate method will return an object of
        type authentication
        so i will cast this object to user that's why in the user class we also implemented the interface
        principle
        * */
        var user = ((User)auth.getPrincipal());
        claims.put("fullName",user.fullName());
        var jwtToken = jwtService.generateToken(claims,user);
     return AuthenticationResponse.builder().token(jwtToken).build();
    }

    @Transactional
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                //todo exception has to be defined
                .orElseThrow(()->new RuntimeException("invalid token")) ;

        //if the token already expired
        if(LocalDateTime.now().isAfter(savedToken.getExpiresAt())){
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired. A new token has been sent to same email address ");
        }else {
            var user =userRepository.findById(savedToken.getUser().getId()).orElseThrow(
                    ()-> new UsernameNotFoundException("user not found")
            );
            user.setEnabled(true);
            userRepository.save(user);
            savedToken.setValidatedAt(LocalDateTime.now());
            tokenRepository.save(savedToken);
        }
    }
}
