package com.project.bookseller.service;

import com.project.bookseller.authentication.UserDetails;
import com.project.bookseller.dto.address.CityDTO;
import com.project.bookseller.dto.address.UserAddressDTO;
import com.project.bookseller.dto.order.OrderInformationDTO;
import com.project.bookseller.dto.order.OrderRecordDTO;
import com.project.bookseller.dto.order.PaymentMethodDTO;
import com.project.bookseller.entity.order.OrderInformation;
import com.project.bookseller.entity.order.OrderRecord;
import com.project.bookseller.entity.location.StockRecord;
import com.project.bookseller.entity.user.address.City;
import com.project.bookseller.entity.book.Book;
import com.project.bookseller.entity.order.OrderStatus;
import com.project.bookseller.entity.order.OrderType;
import com.project.bookseller.entity.order.PaymentMethod;
import com.project.bookseller.entity.user.CartRecord;
import com.project.bookseller.exceptions.DataMismatchException;
import com.project.bookseller.exceptions.NotEnoughStockException;
import com.project.bookseller.exceptions.ResourceNotFoundException;
import com.project.bookseller.repository.*;
import com.project.bookseller.repository.address.CityRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderInformationRepository orderInformationRepository;
    private final UserAddressRepository userAddressRepository;
    private final CartRecordRepository cartRecordRepository;
    private final PaymentService paymentService;
    private final OrderRecordRepository orderRecordRepository;
    private final StockRecordRepository stockRecordRepository;
    private final CityRepository cityRepository;


    //for demonstration purpose only
    private double calculateShipping(UserAddressDTO address) {
        return 0;
    }

    //for demonstration purpose only
    private double calculateDiscount(String coupon) {
        return 0;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = DataMismatchException.class)
    public Map<String, Object> createOrder(UserDetails userDetails, OrderInformationDTO info) throws NotEnoughStockException, UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
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
            OrderInformation orderInformation = new OrderInformation();
            orderInformation.setOrderType(OrderType.ONLINE);
            orderInformation.setUser(userDetails.getUser());
            orderInformation.setOrderStatus(status);
            orderInformation.setPaymentMethod(payment);
            orderInformation.setCity(city);
            orderInformation.setFullAddress(fullAddress);
            orderInformation.setPhone(phone);
            orderInformation.setFullName(fullName);
            orderInformation.setTotal(doubleEstimatedTotal);
            orderInformation.setDiscount(calculatedDiscount);
            orderInformationRepository.save(orderInformation);
            boolean retry = true;
            List<OrderRecord> orderRecords = new ArrayList<>();
            Map<Long, StockRecord> stockRecords = cartRecords.stream().collect(toMap(CartRecord::getCartRecordId, std -> std.getBook().getStockRecords().get(0)));
            for (CartRecord cartRecord : cartRecords) {
                StockRecord stockRecord = stockRecords.get(cartRecord.getCartRecordId());
                final int quantity = itemsMap.get(cartRecord.getCartRecordId()).getQuantity();
                stockRecord.setQuantity(stockRecord.getQuantity() - quantity);
                OrderRecord orderRecord = new OrderRecord();
                while (retry) {
                    try {
                        stockRecordRepository.saveAndFlush(stockRecord);
                    } catch (OptimisticLockException e) {
                        e.printStackTrace();
                        Optional<StockRecord> stockRecordOptional = stockRecordRepository.findStockRecordByStockRecordId(stockRecord.getStockRecordId());
                        if (stockRecordOptional.isEmpty() || stockRecordOptional.get().getQuantity() < quantity) {
                            throw new DataMismatchException("Some items have changed!");
                        }
                        stockRecords.put(stockRecord.getStockRecordId(), stockRecord);
                    }
                    orderRecord.setOrderInformation(orderInformation);
                    orderRecord.setQuantity(quantity);
                    orderRecord.setBook(stockRecord.getBook());
                    orderRecord.setPrice(stockRecord.getBook().getPrice());
                    orderRecordRepository.saveAndFlush(orderRecord);
                    orderRecords.add(orderRecord);
                    retry = false;
                }
                retry = true;
                orderRecords.add(orderRecord);
            }

            cartRecordRepository.deleteAll(cartRecords);
            result.put("items", items);
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

    public List<OrderInformationDTO> getOrders(UserDetails userDetails) {
        List<OrderInformation> orders = orderInformationRepository.findOrderInformationByUserId(userDetails.getUserId());
        return orders.stream().map(OrderInformationDTO::convertFromEntity).toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void clearPendingOrders() {
        List<OrderInformation> orderInformation = orderInformationRepository.findAll();
        for (OrderInformation order : orderInformation) {
            if (order.getOrderStatus() == OrderStatus.PENDING) {
                LocalDateTime createdAt = order.getCreatedAt();
                if (createdAt.isBefore(LocalDateTime.now().minusMinutes(1))) {
                    List<OrderRecord> orderRecords = order.getOrderRecords();
                    for (OrderRecord orderRecord : orderRecords) {
                        boolean retry = true;
                        while (retry) {
                            try {
                                StockRecord stockRecord = orderRecord.getBook().getStockRecords().get(0);
                                stockRecord.setQuantity(orderRecord.getQuantity() + stockRecord.getQuantity());
                                stockRecordRepository.saveAndFlush(stockRecord);
                                retry = false;
                            } catch (OptimisticLockException ignored) {

                            }
                        }
                    }
                    order.setOrderStatus(OrderStatus.CANCELLED);
                    orderInformationRepository.saveAndFlush(order);
                }
            }
        }
    }

    public OrderInformationDTO getOrder(UserDetails userDetails, Long orderInformationId) {
        Optional<OrderInformation> orderInformation = orderInformationRepository.findOrderInformationByOrderInformationId(orderInformationId);
        if (orderInformation.isPresent()) {
            OrderInformation order = orderInformation.get();
            if (Objects.equals(order.getUserId(), userDetails.getUserId())) {
                OrderInformationDTO orderInformationDTO = OrderInformationDTO.convertFromEntity(order);
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
