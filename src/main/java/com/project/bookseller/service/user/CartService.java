package com.project.bookseller.service.user;

import com.project.bookseller.authentication.UserPrincipal;
import com.project.bookseller.dto.CartRecordDTO;
import com.project.bookseller.dto.StockRecordDTO;
import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.dto.order.OrderRecordDTO;
import com.project.bookseller.entity.book.Book;
import com.project.bookseller.entity.location.StockRecord;
import com.project.bookseller.entity.order.OrderRecord;
import com.project.bookseller.entity.order.OrderType;
import com.project.bookseller.entity.user.CartRecord;
import com.project.bookseller.exceptions.DataMismatchException;
import com.project.bookseller.exceptions.NotEnoughStockException;
import com.project.bookseller.exceptions.ResourceNotFoundException;
import com.project.bookseller.repository.CartRecordRepository;
import com.project.bookseller.repository.StockRecordRepository;
import com.project.bookseller.repository.book.BookRepository;
import com.project.bookseller.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class CartService {
    private final CartRecordRepository recordRepository;
    private final CartRecordRepository cartRecordRepository;
    private final StockRecordRepository stockRecordRepository;

    public CartRecordDTO addToCart(UserPrincipal userDetails, CartRecordDTO cartRecordDTO) throws ResourceNotFoundException, NotEnoughStockException {
        StockRecordDTO stockRecordDTO = cartRecordDTO.getStockRecord();
        Optional<StockRecord> optional = stockRecordRepository.findStockRecordByStockRecordId(stockRecordDTO.getStockRecordId());
        if (optional.isPresent()) {
            StockRecord stockRecord = optional.get();
            int stock = stockRecord.getQuantity();
            int quantity = cartRecordDTO.getQuantity();
            CartRecord cartRecord = new CartRecord();
            try {
                if (stock >= quantity) {
                    cartRecord.setQuantity(quantity);
                    cartRecord.setUser(userDetails.getUser());
                    cartRecord.setStockRecord(stockRecord);
                    recordRepository.save(cartRecord);
                    CartRecordDTO response = CartRecordDTO.convertFromEntity(cartRecord);
                    response.getStockRecord().setBook(BookDTO.convertFromEntity(stockRecord.getBook()));
                    return response;
                } else {
                    throw new NotEnoughStockException(NotEnoughStockException.NOT_ENOUGH_STOCK);
                }
            } catch (DataIntegrityViolationException e) {
                List<CartRecord> cartRecords = recordRepository.findCartRecordByUserIdAndStockRecordId(userDetails.getUserId(), stockRecordDTO.getStockRecordId());
                assert cartRecords.size() == 1;
                cartRecord = cartRecords.get(0);
                if (stock >= cartRecord.getQuantity() + quantity) {
                    cartRecord.setQuantity(cartRecord.getQuantity() + quantity);
                    recordRepository.saveAndFlush(cartRecord);
                    return CartRecordDTO.convertFromEntity(cartRecord);
                } else {
                    throw new NotEnoughStockException(NotEnoughStockException.NOT_ENOUGH_STOCK);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw new ResourceNotFoundException("No book found!");
            }
        } else {
            throw new ResourceNotFoundException("No book found!");
        }
    }

    public List<CartRecordDTO> getCart(UserPrincipal userDetails) {

        List<CartRecord> cartRecords = recordRepository.findCartRecordsWithStock(userDetails.getUser().getUserId(), null);
        return cartRecords.stream().map(r -> {
            CartRecordDTO cartRecordDTO = CartRecordDTO.convertFromEntity(r);
            StockRecord stockRecord = r.getStockRecord();
            StockRecordDTO stockRecordDTO = StockRecordDTO.convertFromEntity(stockRecord);
            stockRecordDTO.setBook(BookDTO.convertFromEntity(stockRecord.getBook()));
            cartRecordDTO.setStockRecord(stockRecordDTO);
            return cartRecordDTO;
        }).toList();
    }

    //transactional, delete if all records are from the same user. If not, roll back the transaction.
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = ResourceNotFoundException.class)
    public void deleteFromCart(UserPrincipal userDetails, List<Long> cartRecordIds) throws ResourceNotFoundException {
        List<CartRecord> cartRecords = recordRepository.findCartRecordsWithStock(userDetails.getUser().getUserId(), cartRecordIds);
        for (CartRecord cartRecord : cartRecords) {
            if (cartRecord.getUser().getUserId() == userDetails.getUser().getUserId()) {
                recordRepository.delete(cartRecord);
            } else {
                throw new ResourceNotFoundException(ResourceNotFoundException.NO_SUCH_ITEM);
            }
        }
    }

    @Transactional
    ///increase, decrease
    public CartRecordDTO updateCart(UserPrincipal userDetails, Long cartRecordId, Integer quantity) throws ResourceNotFoundException, NotEnoughStockException {
        List<CartRecord> cartRecords = recordRepository.findCartRecordsWithStock(userDetails.getUser().getUserId(), Collections.singletonList(cartRecordId));
        if (cartRecords.size() == 1) {
            CartRecord cartRecord = cartRecords.get(0);
            StockRecord stockRecord = cartRecord.getStockRecord();
            int stock = stockRecord.getQuantity();
            //if enough quantity or if the request is to decrease the quantity (when a product suddenly out of stock)
            if (quantity <= stock || quantity < cartRecord.getQuantity()) {
                cartRecord.setQuantity(quantity);
                recordRepository.save(cartRecord);
                CartRecordDTO cartRecordDTO = CartRecordDTO.convertFromEntity(cartRecord);
                cartRecordDTO.getStockRecord().setBook(BookDTO.convertFromEntity(stockRecord.getBook()));
                return cartRecordDTO;
            }
            throw new NotEnoughStockException(NotEnoughStockException.NOT_ENOUGH_STOCK);
        }
        throw new ResourceNotFoundException(ResourceNotFoundException.NO_SUCH_ITEM);
    }

    //pre-check checked items to check out
    public List<OrderRecordDTO> preCheck(UserPrincipal userDetails,
                                         List<CartRecordDTO> cartRecordDTOs,
                                         OrderType type) throws NotEnoughStockException {
        List<Long> cartRecordIds = cartRecordDTOs.stream().map(CartRecordDTO::getId).toList();
        List<CartRecord> cartRecords = cartRecordRepository.findCartRecordsWithStock(userDetails.getUserId(), cartRecordIds);
        //some items in the data have been deleted. throw an error
        if (cartRecords.size() != cartRecordDTOs.size() && type.equals(OrderType.PICKUP)) {
            throw new ResourceNotFoundException("Cart Changed!");
        }
        //checking for stock. Use a map to leverage instant get() method.
        List<OrderRecordDTO> response = new ArrayList<>();
        Map<Long, CartRecord> recordMap = new HashMap<>();
        for (CartRecord cartRecord : cartRecords) {
            recordMap.put(cartRecord.getCartRecordId(), cartRecord);
        }
        for (CartRecordDTO cartRecordDTO : cartRecordDTOs) {
            CartRecord cartRecord = recordMap.get(cartRecordDTO.getId());
            if (cartRecord == null) {
                throw new DataMismatchException(DataMismatchException.DATA_MISMATCH);
            }
            StockRecord stockRecord = cartRecord.getStockRecord();
            int stock = stockRecord.getQuantity();
            int quantity = cartRecordDTO.getQuantity();
            Book book = stockRecord.getBook();
            double price = book.getPrice();
            double requestPrice = cartRecordDTO.getStockRecord().getBook().getPrice();
            if (quantity <= stock && OrderService.areDoublesEqual(requestPrice, price)) {
                OrderRecordDTO orderRecordDTO = new OrderRecordDTO();
                orderRecordDTO.setQuantity(quantity);
                StockRecordDTO stockRecordDTO = StockRecordDTO.convertFromEntity(stockRecord);
                stockRecordDTO.setBook(BookDTO.convertFromEntity(book));
                orderRecordDTO.setStockRecord(stockRecordDTO);
                orderRecordDTO.setPrice(price);
                orderRecordDTO.setCartRecordId(cartRecordDTO.getId());
                response.add(orderRecordDTO);
            } else {
                throw new NotEnoughStockException(NotEnoughStockException.NOT_ENOUGH_STOCK);
            }
        }
        return response;
    }

}