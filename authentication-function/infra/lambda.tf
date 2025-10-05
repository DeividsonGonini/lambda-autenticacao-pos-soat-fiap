# Lambda Function
resource "aws_lambda_function" "fast-food-authentication" {
  function_name = "AuthenticationFunction"
  handler       = "authentication.App::handleRequest"
  runtime       = "java17"
  memory_size   = 512
  timeout       = 60
  architectures = ["x86_64"]

  # Usando o JAR gerado no Maven (precisa empacotar antes de rodar o Terraform)
  filename         = "../AuthenticationFunction/target/AuthenticationFunction-1.0-SNAPSHOT.jar"
  source_code_hash = filebase64sha256("../AuthenticationFunction/target/AuthenticationFunction-1.0-SNAPSHOT.jar")

  # Role já existente no AWS Academy (não cria nova role)
  role = "arn:aws:iam::891377152273:role/LabRole"

  environment {
    variables = {
      PARAM1 = "VALUE"
    }
  }
}

# Permissão para API Gateway invocar a Lambda
resource "aws_lambda_permission" "apigw" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.fast-food-authentication.function_name
  principal     = "apigateway.amazonaws.com"
  # source_arn    = "${aws_api_gateway_rest_api.api.execution_arn}/*/*"
}
