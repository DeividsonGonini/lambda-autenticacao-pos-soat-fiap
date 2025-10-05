package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.Map;

public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String USER_POOL_ID = "us-east-1_Z9KXAxv4B";
    private static final String CLIENT_ID = "2pulb1req2tsmglk3th7b5gqcs";
    private static final String CPF_DEFAULT = "00000000000";
    private static final String PASSWORD_DEFAULT = "SenhaForte@123";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        Map<String, Object> responseBody = new HashMap<>();
        int statusCode = 200;

        try (CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.create()) {
            Map<String, String> queryParams = request.getQueryStringParameters();
            String cpf = queryParams != null ? queryParams.get("cpf") : null;
            String username = (cpf != null && !cpf.isBlank()) ? cpf : CPF_DEFAULT;

            // 🔹 Consulta usuário ou cria se não existir
            AdminGetUserResponse userResponse = getOrCreateUser(cognitoClient, username);
            if(!username.equals(CPF_DEFAULT)) {
                responseBody.put("username", userResponse.username());
            }else {
                responseBody.put("username", "unidentified");
            }
            // 🔹 Autenticação
            InitiateAuthResponse authResponse = authenticateUser(cognitoClient, username);
            AuthenticationResultType authResult = authResponse.authenticationResult();

            if (authResult != null) {
                Map<String, String> authMap = new HashMap<>();
                authMap.put("TokenType", authResult.tokenType());
                authMap.put("AccessToken", authResult.accessToken());
                authMap.put("IdToken", authResult.idToken());
                authMap.put("RefreshToken", authResult.refreshToken());

                responseBody.put("authentication", authMap);
            } else {
                responseBody.put("authentication", "No auth result");
            }

        } catch (Exception e) {
            statusCode = 500;
            responseBody.put("error", e.getMessage());
        }

        try {
            String json = objectMapper.writeValueAsString(responseBody);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody(json);
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"error\":\"Falha ao gerar resposta\"}");
        }
    }

    /**
     * Busca usuário, se não existir cria com a senha padrão
     */
    private AdminGetUserResponse getOrCreateUser(CognitoIdentityProviderClient client, String username) {
        try {
            AdminGetUserRequest request = AdminGetUserRequest.builder()
                    .userPoolId(USER_POOL_ID)
                    .username(username)
                    .build();
            return client.adminGetUser(request);

        } catch (UserNotFoundException e) {
            // 🔹 Cria o usuário se não existir
            AdminCreateUserRequest createUserRequest = AdminCreateUserRequest.builder()
                    .userPoolId(USER_POOL_ID)
                    .username(username)
                    .temporaryPassword(PASSWORD_DEFAULT)
                    .messageAction("SUPPRESS") // não envia email/sms
                    .build();

            client.adminCreateUser(createUserRequest);

            // 🔹 Força definir senha permanente
            AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                    .userPoolId(USER_POOL_ID)
                    .username(username)
                    .password(PASSWORD_DEFAULT)
                    .permanent(true)
                    .build();

            client.adminSetUserPassword(setPasswordRequest);

            // 🔹 Retorna o novo usuário
            return client.adminGetUser(AdminGetUserRequest.builder()
                    .userPoolId(USER_POOL_ID)
                    .username(username)
                    .build());
        }
    }

    private InitiateAuthResponse authenticateUser(CognitoIdentityProviderClient client, String username) {
        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .authFlow("USER_PASSWORD_AUTH")
                .clientId(CLIENT_ID)
                .authParameters(Map.of(
                        "USERNAME", username,
                        "PASSWORD", PASSWORD_DEFAULT
                ))
                .build();
        return client.initiateAuth(authRequest);
    }
}
