package com.PortfolioTracker.PortfolioTracker.Controller;

import com.PortfolioTracker.PortfolioTracker.Entity.HoldingEntity;
import com.PortfolioTracker.PortfolioTracker.Service.HoldingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/holdings")
public class HoldingController {
    @Autowired
    private HoldingService holdingService;

    @GetMapping("/{username}")
    public List<HoldingEntity> getHoldingsByUsername(@PathVariable String username) {
        return holdingService.getHoldingsByUsername(username);
    }

    @PostMapping
    public HoldingEntity addHolding(@RequestBody HoldingEntity holding) {
        return holdingService.addHolding(holding);
    }

    @DeleteMapping("/{id}")
    public void deleteHolding(@PathVariable Long id) {
        holdingService.deleteHolding(id);
    }
}
