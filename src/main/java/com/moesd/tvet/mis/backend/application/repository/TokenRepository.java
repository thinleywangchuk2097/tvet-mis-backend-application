package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.Token;


public interface TokenRepository extends JpaRepository<Token, Integer> {
	@Query(value = """
			select t from Token t inner join User u\s
			on t.user.id = u.id\s
			where u.userId = :id and (t.expired = false or t.revoked = false)\s
			""")
	List<Token> findAllValidTokenByUser(String id);

	Optional<Token> findByToken(String token);
	
	void deleteByUser_Id(Long userId);
}
