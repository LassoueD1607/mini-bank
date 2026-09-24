package com.lassoued.accounts.service.impl;

import com.lassoued.accounts.dto.AccountsDto;
import com.lassoued.accounts.dto.CardsDto;
import com.lassoued.accounts.dto.CustomerDetailsDto;
import com.lassoued.accounts.dto.LoansDto;
import com.lassoued.accounts.entity.Accounts;
import com.lassoued.accounts.entity.Customer;
import com.lassoued.accounts.exception.ResourceNotFoundException;
import com.lassoued.accounts.mapper.AccountsMapper;
import com.lassoued.accounts.mapper.CustomerMapper;
import com.lassoued.accounts.repository.AccountsRepository;
import com.lassoued.accounts.repository.CustomerRepository;
import com.lassoued.accounts.service.ICustomersService;
import com.lassoued.accounts.service.client.CardsFeignClient;
import com.lassoued.accounts.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomersServiceImpl implements ICustomersService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    private CardsFeignClient cardsFeignClient;
    private LoansFeignClient loansFeignClient;

    /**
     * @param mobileNumber - Input Mobile Number
     * @return Customer Details based on a given mobileNumber
     */
    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );

        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));

        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(mobileNumber);
        if(loansDtoResponseEntity != null){
            customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());
        }
        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(mobileNumber);
        if(cardsDtoResponseEntity != null){
            customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());
        }

        return customerDetailsDto;

    }
}
