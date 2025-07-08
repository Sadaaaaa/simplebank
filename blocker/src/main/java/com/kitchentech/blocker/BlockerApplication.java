package com.kitchentech.blocker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import lombok.Data;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class BlockerApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlockerApplication.class, args);
    }
}

@RestController
@RequestMapping("/api/blocker")
class BlockerController {
    private final BlockerService blockerService;
    public BlockerController(BlockerService blockerService) { this.blockerService = blockerService; }

    @PostMapping("/check-transfer")
    public ResponseEntity<BlockerResultDto> checkTransfer(@RequestBody BlockerTransferDto dto) {
        BlockerResultDto result = blockerService.check(dto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

@Service
class BlockerService {
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
        // Критерий 2: перевод между новыми пользователями
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

@Data
class BlockerTransferDto {
    private Long fromUserId;
    private Long toUserId;
    private BigDecimal amount;
    private String currency;
}

@Data
class BlockerResultDto {
    private boolean allowed;
    private String reason;
}
