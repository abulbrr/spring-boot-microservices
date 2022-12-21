package com.habbal.orderservice.client;

import com.habbal.orderservice.dto.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping(path = "/api/inventory")
    List<InventoryResponse> isInStock(@RequestParam List<String> skuCode);
}
