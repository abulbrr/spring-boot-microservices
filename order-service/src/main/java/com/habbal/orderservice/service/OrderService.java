package com.habbal.orderservice.service;

import com.habbal.orderservice.dto.OrderLineItemsDto;
import com.habbal.orderservice.dto.OrderRequest;
import com.habbal.orderservice.model.Order;
import com.habbal.orderservice.model.OrderLineItems;
import com.habbal.orderservice.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderLineItemsList(orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToOrder)
                .collect(Collectors.toList()));

        orderRepository.save(order);
        log.info("Order placed");
    }

    private OrderLineItems mapToOrder(OrderLineItemsDto orderLineItemsDto) {
        return OrderLineItems.builder()
                .price(orderLineItemsDto.getPrice())
                .quantity(orderLineItemsDto.getQuantity())
                .skuCode(orderLineItemsDto.getSkuCode())
                .build();
    }
}
