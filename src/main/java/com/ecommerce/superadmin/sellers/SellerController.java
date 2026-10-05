package com.ecommerce.superadmin.sellers;

import com.ecommerce.superadmin.common.PageRequest;
import com.ecommerce.superadmin.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@RestController
@RequestMapping("/api/super-admin/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerRepository sellerRepository;

    @GetMapping
    public PageResponse<SellerSummary> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        String statusFilter = null;
        if (status != null && !status.isBlank()) {
            statusFilter = status.trim().toLowerCase(Locale.ROOT);
            if (!SellerRepository.STATUSES.contains(statusFilter)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unknown status. Use one of: " + String.join(", ", SellerRepository.STATUSES) + ".");
            }
        }
        PageRequest pageRequest = PageRequest.of(page, size);
        return PageResponse.of(
                sellerRepository.search(search, statusFilter, pageRequest),
                pageRequest,
                sellerRepository.count(search, statusFilter));
    }

    @GetMapping("/{id}")
    public SellerDetail get(@PathVariable long id) {
        return sellerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found."));
    }
}
