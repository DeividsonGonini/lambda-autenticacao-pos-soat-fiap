provider "aws" {
  region = "us-east-1"
}

terraform {
  required_version = ">= 1.3.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "= 5.6.2" # ou a versão que você estiver usando
    }
  }
}