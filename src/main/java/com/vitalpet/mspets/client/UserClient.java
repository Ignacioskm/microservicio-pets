package com.vitalpet.mspets.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente hacia ms-users.
 * Sin Eureka, la URL se entrega por configuración (users.service.url / USERS_SERVICE_URL).
 * Solo se invoca si users.validation.enabled=true (ver PetService).
 */
@FeignClient(name = "ms-users", url = "${users.service.url}")
public interface UserClient {

    @GetMapping("/api/user/{id}/exists")
    Boolean existById(@PathVariable("id") Long id);

    @GetMapping("/api/user/{id}/is-client")
    Boolean isClient(@PathVariable("id") Long id);
}
