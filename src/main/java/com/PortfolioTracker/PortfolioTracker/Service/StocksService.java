package com.PortfolioTracker.PortfolioTracker.Service;

import com.PortfolioTracker.PortfolioTracker.Entity.Stocks;
import com.PortfolioTracker.PortfolioTracker.Repository.StocksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StocksService {

    @Autowired
    private StocksRepository stocksRepository;

    public void addStocks(Stocks stocks){
        stocksRepository.save(stocks);
    }

    public List<Stocks> getAllStocks(){
       return stocksRepository.findAll();
    }


}
