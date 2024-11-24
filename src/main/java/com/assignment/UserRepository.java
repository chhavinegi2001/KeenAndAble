package com.assignment;



import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUserNameAndIsDeletedFalse(String userName);

    boolean existsByUserName(String userName);
 
    Optional<User> findByUserName(String userName);
}

