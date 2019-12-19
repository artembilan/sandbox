package org.springframework.integration.jdbc.lock.oracle;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
public class LockOnOracleApplication {

	public static void main(String[] args) {
		SpringApplication.run(LockOnOracleApplication.class, args);
	}

	private final String insertQuery = "INSERT INTO INT_LOCK (REGION, LOCK_KEY, CLIENT_ID, CREATED_DATE) VALUES (?, ?, ?, ?)";

	private final String updateQuery = "UPDATE INT_LOCK SET CREATED_DATE=? WHERE REGION=? AND LOCK_KEY=? AND CLIENT_ID=?";

	private final String region = "DEFAULT";

	private final String id = UUID.randomUUID().toString();

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public boolean acquire(String lock) {
		if (this.jdbcTemplate.update(this.updateQuery, new Date(), this.region, lock, this.id) > 0) {
			return true;
		}
		try {
			return this.jdbcTemplate.update(this.insertQuery, this.region, lock, this.id, new Date()) > 0;
		}
		catch (DuplicateKeyException e) {
			return false;
		}
	}

}
