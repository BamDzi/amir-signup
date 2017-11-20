package pl.testowy.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import pl.testowy.model.User;

@Repository("userRepository")
public interface UserRepository extends CrudRepository<User, Long> {
	User findByEmail(String email);
    User findByConfirmationToken(String confirmationToken);
}
