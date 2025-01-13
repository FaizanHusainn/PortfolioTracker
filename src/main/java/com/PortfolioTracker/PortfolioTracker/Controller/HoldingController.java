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

    @GetMapping("/get-portfolio-value/{username}")
    public double getProfolioValue(@PathVariable String username) throws Exception {
        double portfolioValue = 0.0;
         String API_KEY = "MJ3R01A3CPPV9JTM";
         String BASE_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY";

       List<HoldingEntity> holdings = holdingService.findHoldingsByUsername(username);
       for (HoldingEntity currHolding : holdings){
           String apiUrl = BASE_URL + "&symbol=" + currHolding.getTicker() + "&apikey=" + API_KEY;
            double realtimePrice = holdingService.fetchRealTimePriceWithRetries(apiUrl);
            if(realtimePrice != 0.0){
                portfolioValue += (realtimePrice * currHolding.getQuantity());
                currHolding.setRealtimePrice(realtimePrice);
            }else {
                System.out.println("fetch realtime price fails !");
                portfolioValue += (currHolding.getRealtimePrice() * currHolding.getQuantity());
            }
       }

        return portfolioValue;
    }

}
