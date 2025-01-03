package com.project.bookseller.service;

import com.project.bookseller.authentication.UserPrincipal;
import com.project.bookseller.dto.StockRecordDTO;
import com.project.bookseller.dto.UserDTO;
import com.project.bookseller.dto.address.CityDTO;
import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.dto.order.OrderDTO;
import com.project.bookseller.dto.order.OrderRecordDTO;
import com.project.bookseller.dto.order.PaymentInfo;
import com.project.bookseller.entity.book.Book;
import com.project.bookseller.entity.location.StockRecord;
import com.project.bookseller.entity.order.*;
import com.project.bookseller.entity.user.CartRecord;
import com.project.bookseller.entity.user.address.City;
import com.project.bookseller.exceptions.DataMismatchException;
import com.project.bookseller.exceptions.NotEnoughStockException;
import com.project.bookseller.exceptions.ResourceNotFoundException;
import com.project.bookseller.kafka_producers.OrderEventProducer;
import com.project.bookseller.repository.*;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final OrderRecordRepository orderRecordRepository;
    private final StockRecordRepository stockRecordRepository;
    private final OrderEventProducer orderEventProducer;
    private final CartRecordRepository cartRecordRepository;


    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = DataMismatchException.class)
    public Map<String, Object> createOrder(UserPrincipal userDetails, OrderDTO info) throws NotEnoughStockException, UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        List<OrderRecordDTO> items = info.getItems();
        CityDTO cityDTO = info.getCity();
        City city = new City();
        city.setCityId(cityDTO.getId());
        city.setCityName(cityDTO.getName());
        String longitude = info.getLongitude();
        String latitude = info.getLatitude();
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userDetails.getUserId());
        PaymentInfo paymentInfo = info.getPayment();
        int paymentMethodId = paymentInfo.getPaymentMethodId();
        boolean validateDiscount = false;
        boolean validateShippingFee = false;
        boolean validatePaymentMethod = false;
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
            } else if (Arrays.asList(1, 2, 3).contains(paymentInfo.getSubMethod())) {
                validatePaymentMethod = true;
                payment = PaymentMethod.PREPAID;
                status = OrderStatus.PENDING;
                switch (paymentInfo.getSubMethod()) {
                    case 1 -> vnp_BankCode = "VNPAYQR";
                    case 3 -> vnp_BankCode = "INTCARD";
                    default -> vnp_BankCode = "VNBANK";
                }
            }
        }
//Validate shipping fee
        Double estimatedShipping = info.getEstimatedShippingFee();
        if (estimatedShipping == calculateShipping(longitude, latitude)) {
            validateShippingFee = true;
        }
//Validate items
        List<Long> stockRecordIds = items.stream().map(i -> i.getStockRecord().getStockRecordId()).toList();
        List<StockRecord> stockRecords = stockRecordRepository.findStockRecordsByStockRecordIdIn(stockRecordIds);
        Map<Long, StockRecord> recordMap = stockRecords.stream().collect(Collectors.toMap(StockRecord::getStockRecordId, stockRecord -> stockRecord));
        if (validatePaymentMethod && validateShippingFee && validateDiscount) {
            for (OrderRecordDTO orderRecordDTO : items) {
                int requestQuantity = orderRecordDTO.getQuantity();
                double requestPrice = orderRecordDTO.getPrice();
                StockRecord stockRecord = recordMap.get(orderRecordDTO.getStockRecord().getStockRecordId());
                Book book = stockRecord.getBook();
                Double price = book.getPrice();
                if (!areDoublesEqual(price, requestPrice)) {
                    throw new DataMismatchException(DataMismatchException.DATA_MISMATCH);
                }
                boolean retry = true;
                while (retry) {
                    try {
                        stockRecord.setQuantity(stockRecord.getQuantity() - requestQuantity);
                        stockRecordRepository.saveAndFlush(stockRecord);
                        orderRecordDTO.getStockRecord().setVersion(stockRecord.getVersion());
                        retry = false;
                    } catch (OptimisticLockException e) {
                        e.printStackTrace();
                        Optional<StockRecord> stockRecordOptional = stockRecordRepository.findStockRecordByStockRecordId(stockRecord.getStockRecordId());
                        if (stockRecordOptional.isPresent()) {
                            stockRecord = stockRecordOptional.get();
                        } else {
                            throw new RuntimeException();
                        }
                    } catch (DataIntegrityViolationException e) {
                        throw new NotEnoughStockException(NotEnoughStockException.NOT_ENOUGH_STOCK);
                    }
                }
                total += requestPrice * requestQuantity;
            }
            //after updating stock levels, checking the sum (small deviation acceptable).
            if (!areDoublesEqual(info.getEstimatedTotal(), total)) {
                throw new DataMismatchException(DataMismatchException.DATA_MISMATCH);
            }
            Map<String, Object> result = new HashMap<>();
            if (payment == PaymentMethod.PREPAID) {
                double exchangeRate = getExchangeRate();
                String vnp_Amount = String.valueOf((int) (total * exchangeRate));
                String redirect = paymentService.createRedirectUrl(vnp_Amount, vnp_BankCode);
                result.put("redirect", redirect);
            }
            info.setEmail(userDetails.getEmail());
            info.setTotal(total);
            info.setUserId(userDetails.getUserId());
            info.setOrderStatus(status);
            info.setPaymentMethod(payment);
            List<OrderRecord> orderRecords = new ArrayList<>();
            Order order = Order.convertFromDTO(info);
            order.setOrderRecords(orderRecords);
            order.setUserId(info.getUserId());
            for (OrderRecordDTO orderRecordDTO : items) {
                OrderRecord orderRecord = new OrderRecord();
                StockRecordDTO stockRecordDTO = orderRecordDTO.getStockRecord();
                orderRecord.setOrder(order);
                orderRecord.setStockRecordId(stockRecordDTO.getStockRecordId());
                orderRecord.setBookId(stockRecordDTO.getBook().getBookId());
                orderRecord.setQuantity(orderRecordDTO.getQuantity());
                orderRecord.setPrice(orderRecordDTO.getPrice());
                orderRecords.add(orderRecord);
                CartRecord cartRecord = new CartRecord();
                cartRecord.setCartRecordId(orderRecordDTO.getCartRecordId());
                cartRecordRepository.delete(cartRecord);
            }
            orderRecordRepository.saveAll(orderRecords);
            orderRepository.save(order);
            info.setOrderInformationId(order.getOrderInformationId());
            result.put("order", info);
            return result;
        } else {
            throw new DataMismatchException(DataMismatchException.DATA_MISMATCH);
        }
    }

    private double getExchangeRate() {
        return 26011;
    }

    //for demonstration purpose only
    private double calculateShipping(String longitude, String latitude) {
        return 0;
    }

    //for demonstration purpose only
    private double calculateDiscount(String coupon) {
        return 0;
    }

    //allows for standard deviation
    public static boolean areDoublesEqual(double a, double b) {
        double scaledEpsilon = 1e-6 * Math.max(Math.abs(a), Math.abs(b));
        return Math.abs(a - b) < scaledEpsilon;
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
            orderInformation.setCancelledAt(LocalDateTime.now());
            return OrderDTO.convertFromEntity(orderInformation);

        } else {
            throw new ResourceNotFoundException("Order not found!");

        }
    }

    public List<OrderDTO> getOrders(UserPrincipal userDetails) {
        List<Order> orders = orderRepository.findOrdersByUserId(userDetails.getUserId());
        return orders.stream().map(o -> {
            OrderDTO orderDTO = OrderDTO.convertFromEntity(o);
            List<OrderRecord> orderRecords = o.getOrderRecords();
            for (OrderRecord orderRecord : orderRecords) {
                OrderRecordDTO orderRecordDTO = new OrderRecordDTO();
                BookDTO book = BookDTO.convertFromEntity(orderRecord.getBook());
                orderRecordDTO.setBook(book);
                orderRecordDTO.setQuantity(orderRecord.getQuantity());
                orderRecordDTO.setPrice(orderRecord.getPrice());
                orderDTO.getItems().add(orderRecordDTO);
            }
            return orderDTO;
        }).toList();
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
                order.setCancelledAt(LocalDateTime.now());
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
                }
                return orderInformationDTO;
            }
        }
        throw new ResourceNotFoundException("No order found!");
    }
}
