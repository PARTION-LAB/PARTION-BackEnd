package com.partion.payment.client;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class TossPaymentClient {

    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    private final RestClient restClient = RestClient.create();

    @Value("${toss.secret-key}")
    private String secretKey;

    public TossConfirmResponse confirm(String paymentKey, String orderId, BigDecimal amount) {
        try {
            String authorization = createAuthorizationHeader();

            TossConfirmRequest request =
                    new TossConfirmRequest(paymentKey, orderId, amount);

            return restClient.post()
                    .uri(CONFIRM_URL)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(TossConfirmResponse.class);
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.TOSS_PAYMENT_CONFIRM_FAILED);
        }
    }

    private String createAuthorizationHeader() {
        String value = secretKey + ":";
        String encoded = Base64.getEncoder()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));

        return "Basic " + encoded;
    }

    @Getter
    private static class TossConfirmRequest {

        private final String paymentKey;
        private final String orderId;
        private final BigDecimal amount;

        public TossConfirmRequest(String paymentKey, String orderId, BigDecimal amount) {
            this.paymentKey = paymentKey;
            this.orderId = orderId;
            this.amount = amount;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class TossConfirmResponse {

        private String paymentKey;
        private String orderId;
        private BigDecimal totalAmount;
        private String status;
        private String approvedAt;
    }
}