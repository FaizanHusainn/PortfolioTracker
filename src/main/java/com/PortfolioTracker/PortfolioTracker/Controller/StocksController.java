package com.PortfolioTracker.PortfolioTracker.Controller;


import com.PortfolioTracker.PortfolioTracker.Entity.Stocks;
import com.PortfolioTracker.PortfolioTracker.Service.StocksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stocks")
public class StocksController {

    @Autowired
    private StocksService stocksService;

    @PostMapping
    public void addStocks(@RequestBody Stocks stocks){
        stocksService.addStocks(stocks);
    }

    @GetMapping
    public List<Stocks> getAllStocks(){
       return stocksService.getAllStocks();
    }
}
