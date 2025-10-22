package com.finance.advisor.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.finance.advisor.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
}

// yh jo     {[import org.springframework.data.mongodb.repository.MongoRepository;]}
// hai na

//  yhi main jadu hai yh spring ka sara data utha k mongodb tk le jaa reh 
// sath me    save///find jese 1-2 functions v de re h