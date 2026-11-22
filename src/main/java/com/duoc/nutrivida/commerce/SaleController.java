package com.duoc.nutrivida.commerce;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
public class SaleController {

    private final SaleRepository saleRepository;

    public SaleController(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @GetMapping
    public PagedModel<SaleResponse> list(
            @ParameterObject @PageableDefault(size = 10, sort = "soldAt") Pageable pageable) {
        Page<SaleResponse> page = saleRepository.findAllByOrderBySoldAtDesc(pageable).map(SaleResponse::from);
        return new PagedModel<>(page);
    }
}
