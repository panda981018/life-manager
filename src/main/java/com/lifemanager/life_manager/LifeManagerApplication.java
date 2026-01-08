package com.lifemanager.life_manager;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@EnableJpaAuditing
@SpringBootApplication
public class LifeManagerApplication {

	@PostConstruct
	public void started() {
		// [핵심] 타임존을 한국 시간으로 강제 설정
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		System.out.println("현재 시간: " + new java.util.Date());
	}
	public static void main(String[] args) {
		SpringApplication.run(LifeManagerApplication.class, args);
	}

}
