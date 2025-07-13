package org.example.berichbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // Spring Data JPA의 Auditing 기능 활성화
public class BeRichBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeRichBeApplication.class, args);
    }

}
