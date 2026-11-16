package com.vitalpet.mspets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Microservicio de mascotas (VitalPet Connect) - versión standalone.
 * Ya no se registra en Eureka: se expone directamente por IP/puerto (EC2).
 */
@SpringBootApplication
@EnableFeignClients
public class MsPetsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsPetsApplication.class, args);
    }
}
