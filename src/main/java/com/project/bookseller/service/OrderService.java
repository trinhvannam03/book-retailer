package com.project.bookseller.service;

import com.project.bookseller.authentication.UserPrincipal;
import com.project.bookseller.dto.address.CityDTO;
import com.project.bookseller.dto.address.UserAddressDTO;
import com.project.bookseller.dto.order.OrderDTO;
import com.project.bookseller.dto.order.OrderRecordDTO;
import com.project.bookseller.dto.order.PaymentMethodDTO;
import com.project.bookseller.entity.book.Book;
import com.project.bookseller.entity.location.StockRecord;
import com.project.bookseller.entity.order.*;
import com.project.bookseller.entity.user.CartRecord;
import com.project.bookseller.entity.user.address.City;
import com.project.bookseller.exceptions.DataMismatchException;
import com.project.bookseller.exceptions.NotEnoughStockException;
import com.project.bookseller.exceptions.ResourceNotFoundException;
import com.project.bookseller.repository.*;
import com.project.bookseller.repository.address.CityRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserAddressRepository userAddressRepository;
    private final CartRecordRepository cartRecordRepository;
    private final PaymentService paymentService;
    private final OrderRecordRepository orderRecordRepository;
    private final StockRecordRepository stockRecordRepository;


    //for demonstration purpose only
    private double calculateShipping(UserAddressDTO address) {
        return 0;
    }

    //for demonstration purpose only
    private double calculateDiscount(String coupon) {
        return 0;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = DataMismatchException.class)
    public Map<String, Object> createOrder(UserPrincipal userDetails, OrderDTO info) throws NotEnoughStockException, UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        Map<String, Object> result = new HashMap<>();
        UserAddressDTO userAddress = info.getAddress();
        CityDTO cityDTO = userAddress.getCity();
        City city = new City();
        city.setCityId(cityDTO.getId());
        city.setCityName(cityDTO.getName());
        String fullAddress = userAddress.getFullAddress();
        String phone = userAddress.getPhone();
        String fullName = userAddress.getFullName();
        List<OrderRecordDTO> items = info.getItems();
        PaymentMethodDTO paymentMethod = info.getPayment();
        int paymentMethodId = paymentMethod.getPaymentMethodId();
        boolean validateDiscount = false;
        boolean validateShippingFee = false;
        boolean validatePaymentMethod = false;
        boolean validateItems = false;
        PaymentMethod payment = PaymentMethod.COD;
        OrderStatus status = OrderStatus.PROCESSING;
        double total = 0;
//Validate discount
        Double estimatedDiscount = info.getEstimatedDiscount();
        double calculatedDiscount = calculateDiscount(info.getAppliedCoupon());
        if (calculatedDiscount >= estimatedDiscount) {
            validateDiscount = true;
        }
//Validate payment method
        String vnp_BankCode = null;

        if (Arrays.asList(1000, 1001).contains(paymentMethodId)) {
            if (paymentMethodId == 1000) {
                validatePaymentMethod = true;
            } else if (Arrays.asList(1, 2, 3).contains(paymentMethod.getSubMethod())) {
                validatePaymentMethod = true;
                payment = PaymentMethod.PREPAID;
                status = OrderStatus.PENDING;
                switch (paymentMethod.getSubMethod()) {
                    case 1 -> vnp_BankCode = "VNPAYQR";
                    case 2 -> vnp_BankCode = "VNBANK";
                    case 3 -> vnp_BankCode = "INTCARD";
                }
            }
        }
//Validate shipping fee
        Double estimatedShipping = info.getEstimatedShippingFee();
        Double calculatedShipping = calculateShipping(userAddress);
        if (estimatedShipping.equals(calculatedShipping)) {
            validateShippingFee = true;
        }
//Validate items
//StockRecord Entities are stored in the persistence context.
        List<Long> cartRecordIds = new ArrayList<>();
        Map<Long, OrderRecordDTO> itemsMap = new HashMap<>();
        for (OrderRecordDTO item : items) {
            Long cartRecordId = item.getCartRecordId();
            itemsMap.put(cartRecordId, item);
            cartRecordIds.add(cartRecordId);
        }
        List<CartRecord> cartRecords = cartRecordRepository.findCartRecordsWithStock(userDetails.getUserId(), cartRecordIds);
        //items must be in cart, checkout price must be equal or greater than price, and stock must be sufficient, but quantity in cart may vary.
        if (cartRecords.size() == items.size()) {
            for (CartRecord cartRecord : cartRecords) {
                Book book = cartRecord.getBook();
                OrderRecordDTO item = itemsMap.get(cartRecord.getCartRecordId());
                Double price = book.getPrice();
                Double checkOutPrice = item.getPrice();
                int quantity = item.getQuantity();
                List<StockRecord> stockRecords = book.getStockRecords();
                total += ((Integer) itemsMap.get(cartRecord.getCartRecordId()).getQuantity()) * cartRecord.getBook().getPrice();
                //online orders are shipped from one single location. The list stockRecords only contains one record.
                StockRecord stockRecord = stockRecords.get(0);
                int stock = stockRecord.getQuantity();
                if (checkOutPrice.equals(price) && stock < quantity) {
                    throw new DataMismatchException("Some items have changed!");
                }
            }
        } else {
            throw new DataMismatchException("Some items have changed!");
        }
        String totalToFixed2 = String.format("%.2f", total);
        String estimatedTotal = String.valueOf(info.getEstimatedTotal());

        final double doubleTotal = Double.parseDouble(totalToFixed2);
        final double doubleEstimatedTotal = Double.parseDouble(estimatedTotal);
        if (doubleTotal == doubleEstimatedTotal) {
            validateItems = true;
        }
        if (validatePaymentMethod && validateItems && validateShippingFee && validateDiscount) {
            Order order = new Order();
            order.setOrderType(OrderType.ONLINE);
            order.setUser(userDetails.getUser());
            order.setOrderStatus(status);
            order.setPaymentMethod(payment);
            order.setCity(city);
            order.setFullAddress(fullAddress);
            order.setPhone(phone);
            order.setFullName(fullName);
            order.setTotal(doubleEstimatedTotal);
            order.setDiscount(calculatedDiscount);
            orderRepository.save(order);
            Map<Long, StockRecord> stockRecords = cartRecords.stream().collect(toMap(CartRecord::getCartRecordId, std -> std.getBook().getStockRecords().get(0)));
            for (CartRecord cartRecord : cartRecords) {
                StockRecord stockRecord = stockRecords.get(cartRecord.getCartRecordId());
                final int orderedQuantity = itemsMap.get(cartRecord.getCartRecordId()).getQuantity();//per item
                stockRecord.setQuantity(stockRecord.getQuantity() - orderedQuantity);
                OrderRecord orderRecord = new OrderRecord();
                boolean retry = true;
                do {
                    try {
                        stockRecordRepository.saveAndFlush(stockRecord);
                        retry = false;
                    } catch (OptimisticLockException e) {
                        e.printStackTrace();
                        Optional<StockRecord> stockRecordOptional = stockRecordRepository.findStockRecordByStockRecordId(stockRecord.getStockRecordId());
                        //recheck new stockRecord entity instance;
                        if (stockRecordOptional.isEmpty() || stockRecordOptional.get().getQuantity() < orderedQuantity) {
                            throw new DataMismatchException("Some items have changed!");
                        }
                        //update new instance of StockRecord;
                        StockRecord newStockRecord = stockRecordOptional.get();
                        newStockRecord.setQuantity(newStockRecord.getQuantity() - orderedQuantity);
                        stockRecords.put(stockRecord.getStockRecordId(), newStockRecord);
                    }
                    //still true, retry the operation with new stock record
                } while (retry);
                //after successfully updating stock levels, save orderRecords and save the order;
                orderRecord.setOrderInformation(order);
                orderRecord.setQuantity(orderedQuantity);
                orderRecord.setStockRecord(stockRecord);
                orderRecord.setBook(stockRecord.getBook());
                orderRecord.setPrice(stockRecord.getBook().getPrice());
                orderRecordRepository.saveAndFlush(orderRecord);
            }
            //after saving all items in order, delete all in cart;
            cartRecordRepository.deleteAll(cartRecords);
            //create redirect link to pay;
            if (vnp_BankCode != null) {
                String vnp_Amount = String.valueOf((int) (doubleTotal * 25000));
                String redirect = paymentService.createRedirectUrl(vnp_Amount, vnp_BankCode);
                result.put("redirect", redirect);
            }
            result.put("message", "Order created successfully");
            return result;
        } else {
            throw new DataMismatchException("Some items have changed!");
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderDTO cancelOrder(UserPrincipal userDetails, Long orderInformationId) {
        Optional<Order> orderInformationOptional = orderRepository.findOrderWithStockByOrderInformationId(userDetails.getUserId(), orderInformationId);
        if (orderInformationOptional.isPresent()) {
            Order orderInformation = orderInformationOptional.get();
            List<OrderRecord> orderRecords = orderInformation.getOrderRecords();
            for (OrderRecord orderRecord : orderRecords) {
                boolean retry = true;
                StockRecord stockRecord = orderRecord.getStockRecord();
                do {
                    try {
                        stockRecord.setQuantity(stockRecord.getQuantity() + orderRecord.getQuantity());
                        stockRecordRepository.saveAndFlush(stockRecord);
                        retry = false;
                    } catch (OptimisticLockException ignored) {
                    }
                } while (retry);
            }
            orderRepository.saveAndFlush(orderInformation);
            orderInformation.setOrderStatus(OrderStatus.CANCELLED);
            return OrderDTO.convertFromEntity(orderInformation);

        } else {
            throw new ResourceNotFoundException("Order not found!");

        }
    }

    public List<OrderDTO> getOrders(UserPrincipal userDetails) {
        List<Order> orders = orderRepository.findOrdersByUserId(userDetails.getUserId());
        return orders.stream().map(OrderDTO::convertFromEntity).toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void cancelPendingOrders() {
        List<Order> orderInformation = orderRepository.findAllOrderByOrderStatus(OrderStatus.PENDING);
        for (Order order : orderInformation) {
            LocalDateTime createdAt = order.getCreatedAt();
            LocalDateTime threshold = LocalDateTime.now().minusSeconds(20);
            if (createdAt.isBefore(threshold)) {
                List<OrderRecord> orderRecords = order.getOrderRecords();

                for (OrderRecord orderRecord : orderRecords) {
                    boolean retry = true;
                    do {
                        try {
                            //Ensure the item's stock is returned to the location where it was bought
                            StockRecord stockRecord = orderRecord.getStockRecord();
                            stockRecord.setQuantity(stockRecord.getQuantity() + orderRecord.getQuantity());
                            stockRecordRepository.saveAndFlush(stockRecord);
                            retry = false;
                        } catch (OptimisticLockException ignored) {

                        }
                    } while (retry);
                }
                order.setOrderStatus(OrderStatus.CANCELLED);
                orderRepository.saveAndFlush(order);
            }
        }
    }

    public OrderDTO getOrder(UserPrincipal userDetails, Long orderInformationId) {
        Optional<Order> orderInformation = orderRepository.findOrderByOrderInformationId(userDetails.getUserId(), orderInformationId);
        if (orderInformation.isPresent()) {
            Order order = orderInformation.get();
            if (Objects.equals(order.getUserId(), userDetails.getUserId())) {
                OrderDTO orderInformationDTO = OrderDTO.convertFromEntity(order);
                for (OrderRecord orderRecord : order.getOrderRecords()) {
                    OrderRecordDTO orderRecordDTO = OrderRecordDTO.convertFromEntity(orderRecord);
                    orderInformationDTO.getItems().add(orderRecordDTO);
                }
                return orderInformationDTO;
            }
        }
        throw new ResourceNotFoundException("No order found!");
    }
}
