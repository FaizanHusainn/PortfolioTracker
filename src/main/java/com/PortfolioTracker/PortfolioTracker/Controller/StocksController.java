package com.PortfolioTracker.PortfolioTracker.Controller;


import com.PortfolioTracker.PortfolioTracker.Entity.HoldingEntity;
import com.PortfolioTracker.PortfolioTracker.Entity.Stocks;
import com.PortfolioTracker.PortfolioTracker.Service.HoldingService;
import com.PortfolioTracker.PortfolioTracker.Service.StocksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/stocks")
public class StocksController {

    @Autowired
    private StocksService stocksService;

    @Autowired
    private HoldingService holdingService;

    @Autowired
    private HoldingController holdingController;

    @PostMapping
    public void addStocks(@RequestBody Stocks stocks){
        stocksService.addStocks(stocks);
    }

    @GetMapping
    public List<Stocks> getAllStocks(){
       return stocksService.getAllStocks();
    }

    @PostMapping("/{username}")
    public List<Stocks> addStocksToNewUser(@PathVariable String username) throws Exception {
        List<Stocks> stocks = stocksService.getAllStocks();
        String API_KEY = "MJ3R01A3CPPV9JTM";
        String BASE_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY";

        for(int i=0; i<5; i++){
            Random random = new Random();
            int randomNumber = random.nextInt(8);
            Stocks randomStock = stocks.get(randomNumber);
            String apiUrl = BASE_URL + "&symbol=" + randomStock.getTicker() + "&apikey=" + API_KEY;
            System.out.println("Ticker in random stock : "+ randomStock.getTicker());
          //  double realtimePrice = holdingService.fetchRealTimePriceWithRetries(apiUrl);
            //creating a holding Entity;
            HoldingEntity currHolding = new HoldingEntity();
            currHolding.setUsername(username);
            currHolding.setStock(randomStock.getStockname());
            currHolding.setTicker(randomStock.getTicker());
            currHolding.setTicker("1");
         //   currHolding.setBuyPrice(realtimePrice);
//            currHolding.setRealtimePrice(realtimePrice);
//            currHolding.setTotalValue(realtimePrice);

            HoldingEntity returnEntity = holdingController.addHolding(currHolding);

        }
        return stocks;
    }
}
