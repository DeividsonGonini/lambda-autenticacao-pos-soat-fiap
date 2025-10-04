//package helloworld;
//
//import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.RequestHandler;
//import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
//import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
//import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class App1 implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
//
//    private static final String USER_POOL_ID = "us-east-1_Z9KXAxv4B";
//    private static final String CLIENT_ID = "2pulb1req2tsmglk3th7b5gqcs";
//    private static final String CPF_DEFAULT = "00000000000";
//    private static final String PASSWORD_DEFAULT = "SenhaForte@123";
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
//        Map<String, Object> responseBody = new HashMap<>();
//        int statusCode = 200;
//
//        try (CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.create()) {
//            Map<String, String> queryParams = request.getQueryStringParameters();
//            String cpf = queryParams != null ? queryParams.get("cpf") : null;
//
//            // 🔹 Consulta usuário
//            AdminGetUserResponse userResponse = getUserFromCognito(cognitoClient, cpf);
//            responseBody.put("username", userResponse.username());
//
//            // 🔹 Autenticação
//            InitiateAuthResponse authResponse = authenticateUser(cognitoClient, cpf != null ? cpf : CPF_DEFAULT);
//            AuthenticationResultType authResult = authResponse.authenticationResult();
//
//            if (authResult != null) {
//                Map<String, String> authMap = new HashMap<>();
//                authMap.put("TokenType", authResult.tokenType());
//                authMap.put("AccessToken", authResult.accessToken());
//                authMap.put("IdToken", authResult.idToken());
//                authMap.put("RefreshToken", authResult.refreshToken());
//
//                responseBody.put("authentication", authMap);
//            } else {
//                responseBody.put("authentication", "No auth result");
//            }
//
//        } catch (Exception e) {
//            statusCode = 500;
//            responseBody.put("error", e.getMessage());
//        }
//
//        try {
//            String json = objectMapper.writeValueAsString(responseBody);
//            return new APIGatewayProxyResponseEvent()
//                    .withStatusCode(statusCode)
//                    .withHeaders(Map.of("Content-Type", "application/json"))
//                    .withBody(json);
//        } catch (Exception e) {
//            return new APIGatewayProxyResponseEvent()
//                    .withStatusCode(500)
//                    .withBody("{\"error\":\"Falha ao gerar resposta\"}");
//        }
//    }
//
//    private AdminGetUserResponse getUserFromCognito(CognitoIdentityProviderClient client, String cpf) {
//        String username = (cpf != null && !cpf.isBlank()) ? cpf : CPF_DEFAULT;
//        AdminGetUserRequest request = AdminGetUserRequest.builder()
//                .userPoolId(USER_POOL_ID)
//                .username(username)
//                .build();
//        return client.adminGetUser(request);
//    }
//
//    private InitiateAuthResponse authenticateUser(CognitoIdentityProviderClient client, String username) {
//        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
//                .authFlow("USER_PASSWORD_AUTH")
//                .clientId(CLIENT_ID)
//                .authParameters(Map.of(
//                        "USERNAME", username,
//                        "PASSWORD", PASSWORD_DEFAULT
//                ))
//                .build();
//        return client.initiateAuth(authRequest);
//    }
//}
