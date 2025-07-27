package com.kitchentech.blocker.service;

import com.kitchentech.blocker.dto.BlockerResultDto;
import com.kitchentech.blocker.dto.BlockerTransferDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Service
public class BlockerService {
    private final Set<String> knownPairs = new HashSet<>();
    private static final BigDecimal LIMIT = new BigDecimal("100000");

    public BlockerResultDto check(BlockerTransferDto dto) {
        BlockerResultDto result = new BlockerResultDto();
        result.setAllowed(true);
        result.setReason("Разрешено");
        // Критерий 1: сумма превышает лимит
        if (dto.getAmount().compareTo(LIMIT) > 0) {
            result.setAllowed(false);
            result.setReason("Сумма перевода превышает лимит 100000");
            return result;
        }

        String pair = dto.getFromUserId() + "->" + dto.getToUserId();
        if (!knownPairs.contains(pair)) {
            knownPairs.add(pair);
            result.setAllowed(false);
            result.setReason("Перевод между пользователями, которые ранее не переводили друг другу");
            return result;
        }
        return result;
    }
}
