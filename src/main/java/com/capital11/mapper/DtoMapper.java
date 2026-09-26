package com.capital11.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.capital11.dto.request.RegisterRequest;
import com.capital11.dto.request.RegistrationForm;
import com.capital11.dto.response.AccountResponse;
import com.capital11.dto.response.CustomerResponse;
import com.capital11.dto.response.TransactionResponse;
import com.capital11.domain.Account;
import com.capital11.domain.BankTransaction;
import com.capital11.domain.Customer;

/**
 * Maps entities to the responses used by the JSON API and the Thymeleaf pages, and registration input to a new
 * {@link Customer}; entities never reach either layer directly.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DtoMapper {

    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "type", source = "acctType")
    AccountResponse toResponse(Account account);

    TransactionResponse toResponse(BankTransaction transaction);

    List<TransactionResponse> toTransactionResponses(List<BankTransaction> transactions);

    /** The cid is assigned by {@code BankService.register}. */
    @Mapping(target = "cid", ignore = true)
    @Mapping(target = "birthday", expression = "java(form.birthday())")
    Customer toCustomer(RegistrationForm form);

    /** The cid is assigned by {@code BankService.register}. */
    @Mapping(target = "cid", ignore = true)
    @Mapping(target = "birthday", expression = "java(request.legacyBirthday())")
    Customer toCustomer(RegisterRequest request);
}
