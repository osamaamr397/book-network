package com.amr.book.config;

import com.amr.book.user.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
//integer as the user id of type integer
//and as that the class doesn't used yet will declare it as bean in BeansConfig class
public class ApplicationAuditAware implements AuditorAware<Integer> {
    @Override
    public Optional<Integer> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null ||
            !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken
                //we don't know the user yet
        ){
            return Optional.empty();
        }
        User userPrinciple = (User) authentication.getPrincipal();
        return Optional.ofNullable(userPrinciple.getId());
        //ofNullable in case of that userPrinciple is null
    }
}
