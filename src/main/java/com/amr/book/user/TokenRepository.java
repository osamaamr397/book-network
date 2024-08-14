package com.amr.book.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token,Integer> {
    //optional is rapper to null not found Exception
    Optional<Token>findByToken(String token);
}
