package com.kitchentech.blocker.service;

import com.kitchentech.blocker.dto.BlockerResultDto;
import com.kitchentech.blocker.dto.BlockerTransferDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BlockerService {
    private static final BigDecimal LIMIT = new BigDecimal("100000");

    public BlockerResultDto check(BlockerTransferDto dto) {
        BlockerResultDto result = new BlockerResultDto();
        result.setAllowed(true);
        result.setReason("Разрешено");

        if (dto.getAmount().compareTo(LIMIT) > 0) {
            result.setAllowed(false);
            result.setReason("Сумма перевода превышает лимит 100000");
            return result;
        }
        return  result;
    }
}
