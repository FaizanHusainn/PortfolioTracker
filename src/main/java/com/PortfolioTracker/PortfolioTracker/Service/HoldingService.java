package com.PortfolioTracker.PortfolioTracker.Service;

import com.PortfolioTracker.PortfolioTracker.Entity.HoldingEntity;
import com.PortfolioTracker.PortfolioTracker.Repository.HoldingRepository;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Data
@Getter
@Setter
public class HoldingService {
    @Autowired
    private HoldingRepository holdingRepository;

//    private final String API_KEY = "QRYJ8K9REXK5QNSX";
    private final String API_KEY = "MJ3R01A3CPPV9JTM";
    private final String BASE_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY";

    private RestTemplate restTemplate = new RestTemplate();

    private final int MAX_RETRIES = 5; // Number of retry attempts

    public HoldingEntity addHolding(HoldingEntity holding) {
        String apiUrl = BASE_URL + "&symbol=" + holding.getTicker() + "&apikey=" + API_KEY;

        try {
            // Retry fetching the real-time price
            double realtimePrice = fetchRealTimePriceWithRetries(apiUrl);
            holding.setRealtimePrice(realtimePrice);
            holding.setTotalValue(realtimePrice * holding.getQuantity());
            if(holding.getBuyPrice() == 0.0){
                holding.setBuyPrice(realtimePrice);
            }
        } catch (Exception e) {
            // Handle errors and set default values
            System.err.println("Error fetching real-time price: " + e.getMessage());
            holding.setRealtimePrice(0.0);
            holding.setTotalValue(0.0);
        }

        // Save the holding with updated values
        return holdingRepository.save(holding);
    }


    public double fetchRealTimePriceWithRetries(String apiUrl) throws Exception {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                var response = restTemplate.getForObject(apiUrl, Map.class);
                if (response != null && response.containsKey("Time Series (Daily)")) {
                    // Parse the latest date and price
                    String latestDate = ((Map<String, Object>) response.get("Time Series (Daily)")).keySet().iterator().next();
                    double realtimePrice = Double.parseDouble(
                            ((Map<String, Object>) ((Map<String, Object>) response.get("Time Series (Daily)")).get(latestDate)).get("4. close").toString()
                    );
                    return realtimePrice; // Return the fetched price
                } else {
                    throw new Exception("Invalid response structure");
                }
            } catch (Exception e) {
                attempt++;
                System.err.println("Retrying API call (" + attempt + "/" + MAX_RETRIES + "): " + e.getMessage());
                if (attempt >= MAX_RETRIES) {
                    throw new Exception("Failed to fetch real-time price after " + MAX_RETRIES + " attempts");
                }
                Thread.sleep(1000); // Delay between retries (1 second)
            }
        }
        return 0.0; // Fallback value
    }

    public List<HoldingEntity> getHoldingsByUsername(String username) {
        List<HoldingEntity> holdings = holdingRepository.findByUsername(username);

        for(HoldingEntity e : holdings){
            System.out.println("Username : " +e.getUsername());
            System.out.println("Realtime price : " +e.getRealtimePrice());
            System.out.println("Buy Price : " +e.getBuyPrice());
            System.out.println("Total Value : "+e.getTotalValue());
        }

        return holdings;
    }


    public void deleteHolding(Long id) {
        holdingRepository.deleteById(id);
    }

    public List<HoldingEntity> findHoldingsByUsername(String username){
       return holdingRepository.findByUsername(username);
    }
}
