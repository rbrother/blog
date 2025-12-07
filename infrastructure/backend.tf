# Remote state backend for Terraform
# This stores the state file in S3, allowing it to sync across machines
terraform {
  backend "s3" {
    bucket         = "brotherus-blog-terraform-state"
    key            = "terraform.tfstate"
    region         = "eu-north-1"
    encrypt        = true
    dynamodb_table = "brotherus-blog-terraform-locks"
  }
}
