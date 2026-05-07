package app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	// Spring Data JPA es tan inteligente que solo con leer "findByEmail", 
	// ya sabe que tiene que hacer un "SELECT * FROM users WHERE email = ?" en MySQL.
	Optional<User> findByEmail(String email); 
}