package com.vitalpet.mspets.client;

import com.vitalpet.mspets.config.InternalFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "ms-users",
    url = "${users.service.url}",
    configuration = InternalFeignConfig.class
)
public interface UserClient {

    @GetMapping("/api/users/{id}/exists")
    Boolean existById(@PathVariable("id") Long id);

    @GetMapping("/api/users/{id}/is-client")
    Boolean isClient(@PathVariable("id") Long id);
}
