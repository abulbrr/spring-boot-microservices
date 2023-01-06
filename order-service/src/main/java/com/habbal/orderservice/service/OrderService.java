package com.habbal.orderservice.service;

import com.habbal.orderservice.client.InventoryClient;
import com.habbal.orderservice.dto.InventoryResponse;
import com.habbal.orderservice.dto.OrderLineItemsDto;
import com.habbal.orderservice.dto.OrderRequest;
import com.habbal.orderservice.event.OrderPlacedEvent;
import com.habbal.orderservice.model.Order;
import com.habbal.orderservice.model.OrderLineItems;
import com.habbal.orderservice.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderLineItemsList(orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToOrder)
                .collect(Collectors.toList()));


        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        List<InventoryResponse> inventoryItems = inventoryClient.isInStock(skuCodes);
        boolean productNotInStock = inventoryItems
                .stream()
                .anyMatch(inventoryResponse -> !inventoryResponse.isInStock());

        if(CollectionUtils.isEmpty(inventoryItems) || productNotInStock) {
            throw new NoSuchElementException("Product not found in inventory!");
        }

        orderRepository.save(order);
        kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
        log.info("Order placed");
        return "Order Placed Successfully";
    }

    private OrderLineItems mapToOrder(OrderLineItemsDto orderLineItemsDto) {
        return OrderLineItems.builder()
                .price(orderLineItemsDto.getPrice())
                .quantity(orderLineItemsDto.getQuantity())
                .skuCode(orderLineItemsDto.getSkuCode())
                .build();
    }
}
