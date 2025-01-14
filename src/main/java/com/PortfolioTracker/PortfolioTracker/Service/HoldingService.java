package com.PortfolioTracker.PortfolioTracker.Service;

import com.PortfolioTracker.PortfolioTracker.Entity.HoldingEntity;
import com.PortfolioTracker.PortfolioTracker.Exception.ApiException;
import com.PortfolioTracker.PortfolioTracker.Repository.HoldingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final RestTemplate restTemplate;

    private static final int MAX_RETRIES = 6; // Number of retry attempts
    private static final String API_KEY = "MJ3R01A3CPPV9JTM"; // Replace with your API key
    private static final String BASE_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY";

    @Autowired
    public HoldingService(HoldingRepository holdingRepository, RestTemplate restTemplate) {
        this.holdingRepository = holdingRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Adds a new holding and fetches the real-time price for the ticker.
     *
     * @param holding The holding entity to add.
     * @return The saved holding entity with updated real-time price and total value.
     */
    public HoldingEntity addHolding(HoldingEntity holding) {
        String apiUrl = BASE_URL + "&symbol=" + holding.getTicker() + "&apikey=" + API_KEY;

        try {
            // Fetch the real-time price with retries
            double realtimePrice = fetchRealTimePriceWithRetries(apiUrl);
            holding.setRealtimePrice(realtimePrice);
            holding.setTotalValue(realtimePrice * holding.getQuantity());

            // Set the buy price if not already set
            if (holding.getBuyPrice() == 0.0) {
                holding.setBuyPrice(realtimePrice);
            }
        } catch (ApiException e) {
            // Handle errors and set default values
            System.err.println("Error fetching real-time price: " + e.getMessage());
            holding.setRealtimePrice(0.0);
            holding.setTotalValue(0.0);
        }

        // Save the holding with updated values
        return holdingRepository.save(holding);
    }

    /**
     * Fetches the real-time price for a given ticker using the Alpha Vantage API.
     * Implements a retry mechanism in case of failures.
     *
     * @param apiUrl The API URL to fetch the real-time price.
     * @return The real-time price as a double.
     * @throws ApiException If the price cannot be fetched after retries.
     */
    @Retryable(
            value = {ApiException.class}, // Retry on ApiException
            maxAttempts = MAX_RETRIES,    // Maximum number of retries
            backoff = @Backoff(delay = 1000) // Delay between retries (1 second)
    )
    public double fetchRealTimePriceWithRetries(String apiUrl) throws ApiException {
        try {
            // Fetch the API response
            ResponseEntity<Map> responseEntity = restTemplate.getForEntity(apiUrl, Map.class);

            // Check if the response is successful and has a body
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                Map<String, Object> response = responseEntity.getBody();

                // Check if the response contains the expected data
                if (response.containsKey("Time Series (Daily)")) {
                    // Extract the latest date and closing price
                    Map<String, Object> timeSeriesDaily = (Map<String, Object>) response.get("Time Series (Daily)");
                    String latestDate = timeSeriesDaily.keySet().iterator().next();
                    Map<String, Object> latestData = (Map<String, Object>) timeSeriesDaily.get(latestDate);
                    String closingPrice = latestData.get("4. close").toString();

                    // Parse and return the closing price
                    return Double.parseDouble(closingPrice);
                } else {
                    throw new ApiException("Invalid response structure: 'Time Series (Daily)' not found");
                }
            } else {
                throw new ApiException("Failed to fetch data: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            throw new ApiException("Failed to fetch real-time price: " + e.getMessage(), e);
        }
    }


    public List<HoldingEntity> getHoldingsByUsername(String username) {
        List<HoldingEntity> holdings = holdingRepository.findByUsername(username);

        // Print holding details (for debugging purposes)
        for (HoldingEntity e : holdings) {
            System.out.println("Username: " + e.getUsername());
            System.out.println("Realtime price: " + e.getRealtimePrice());
            System.out.println("Buy Price: " + e.getBuyPrice());
            System.out.println("Total Value: " + e.getTotalValue());
        }

        return holdings;
    }

    /**
     * Deletes a holding by its ID.
     *
     * @param id The ID of the holding to delete.
     */
    public void deleteHolding(Long id) {
        holdingRepository.deleteById(id);
    }

    /**
     * Finds holdings by username.
     *
     * @param username The username to filter holdings.
     * @return A list of holdings for the specified username.
     */
    public List<HoldingEntity> findHoldingsByUsername(String username) {
        return holdingRepository.findByUsername(username);
    }
}