package com.ridelink.ride.client;

import com.ridelink.ride.dto.AccountResponse;
import com.ridelink.ride.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AccountClient {

    private final RestClient restClient;

    public AccountClient(
            @Value("${app.account-service.base-url}") String baseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public AccountResponse getCurrentAccount(String bearerToken) {

        try {
            AccountResponse response = restClient.get()
                    .uri("/api/accounts/me")
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + bearerToken)
                    .retrieve()
                    .body(AccountResponse.class);

            if (response == null || response.getId() == null) {
                throw new ApiException(
                        HttpStatus.BAD_GATEWAY,
                        "Invalid response from Account Service");
            }

            return response;

        } catch (HttpClientErrorException.Unauthorized exception) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Account Service rejected authentication");

        } catch (HttpClientErrorException.Forbidden exception) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Account is not allowed to create a ride");

        } catch (ApiException exception) {
            throw exception;

        } catch (RestClientException exception) {
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Account Service is unavailable");
        }
    }
}