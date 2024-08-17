package com.amr.book.user;

import com.amr.book.role.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static jakarta.persistence.FetchType.EAGER;


@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "_user")
@EntityListeners(AuditingEntityListener.class) //used for is created at ,lastModifiedDate and so on
//when use it we should goto application and add @EnableJpaAuditing
public class User implements UserDetails, Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String firstname;
    private String lastname;
    private LocalDate dateOfBirth;
    @Column(unique = true)
    private String email;
    private String password;
    private boolean accountLocked;
    private boolean enabled;
    @ManyToMany(fetch = EAGER)
    private List<Role> roles;
    @CreatedDate
    @Column(nullable = false,updatable = false)
    //this createdDate should not be null always to be set
    //and updatable false as we want to keep track when this object is created but when want to update it
    // we don't to update this one
    private LocalDateTime createdDate;
    @CreatedDate
    @Column(insertable = false)
    //insertable false means when we create and new record
    // we don't want to insert or initialize the value of this attribute
    private LocalDateTime lastModifiedDate;
    @Override
    public String getName() {
        //we consider that our unique
        // identifier is the email as getName get the unique identifier
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles
                .stream()
                .map(r -> new SimpleGrantedAuthority(r.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
    //always think about implementing UserDetails as spring sec will look for
    // or create an object of type UserDetails
    //authentication also need principle
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
    public String fullName(){
        return firstname + " " + lastname;
    }

}
