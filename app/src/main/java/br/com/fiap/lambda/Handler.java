package br.com.fiap.lambda;

import br.com.fiap.lambda.response.ClienteResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import br.com.fiap.lambda.request.LoginRequest;
import br.com.fiap.lambda.response.LoginResponse;

import java.util.Objects;

public class Handler implements
        RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static  ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request,
                                                      Context context) {
        var logger = context.getLogger();


            logger.log("Request received - " + request.getBody());

            boolean isAuthorized = false;

        LoginRequest login = null;
        try {
            login = objectMapper.readValue(request.getBody(), LoginRequest.class);

            String url = System.getenv("api");
            
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<ClienteResponse> response = restTemplate.getForEntity(url + "v1/clientes" + login.cpf(), ClienteResponse.class);

            if (Objects.nonNull(response.getBody())) {
                isAuthorized = true;
            }

            var loginResponse = new LoginResponse(isAuthorized);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(objectMapper.writeValueAsString(loginResponse))
                    .withIsBase64Encoded(false);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }

}
