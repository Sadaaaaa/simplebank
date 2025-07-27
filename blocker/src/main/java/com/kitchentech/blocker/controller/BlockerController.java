package com.kitchentech.blocker.controller;


import com.kitchentech.blocker.service.BlockerService;
import com.kitchentech.blocker.dto.BlockerResultDto;
import com.kitchentech.blocker.dto.BlockerTransferDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blocker")
public class BlockerController {
    private final BlockerService blockerService;

    public BlockerController(BlockerService blockerService) {
        this.blockerService = blockerService;
    }

    @PostMapping("/check-transfer")
    public ResponseEntity<BlockerResultDto> checkTransfer(@RequestBody BlockerTransferDto dto) {
        BlockerResultDto result = blockerService.check(dto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
