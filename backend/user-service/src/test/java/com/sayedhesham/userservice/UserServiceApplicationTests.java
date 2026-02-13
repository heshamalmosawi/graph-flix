package com.sayedhesham.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

@SpringBootTest
class UserServiceApplicationTests {

	@Autowired(required = false)
	private Neo4jClient neo4jClient;

	@Test
	void contextLoads() {
	}

	@Test
	void neo4jConnectionTest() {
		if (neo4jClient != null) {
			System.out.println("Neo4j Client initialized successfully!");
		} else {
			System.out.println("Neo4j Client not available");
		}
	}
}
