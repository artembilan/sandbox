package org.springframework.integration.jdbc.lock.oracle;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class LockOnOracleApplicationTests {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	LockOnOracleApplication lockOnOracleApplication;

	@BeforeEach
	void setup() {
		this.jdbcTemplate.update("TRUNCATE TABLE INT_LOCK");
	}

	@Test
	void contextLoads() {
		assertThat(this.lockOnOracleApplication.acquire("foo")).isTrue();
		assertThat(this.lockOnOracleApplication.acquire("foo")).isTrue();
	}

}
