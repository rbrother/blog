# Building Programs Blog

This is the source code for the "Building Programs" blog by Robert J. Brotherus.

## Tech Stack

* **Architecture**: Server-Side Rendered (SSR) AWS Lambda function
* **Languages**: [ClojureScript](https://clojurescript.org/) compiled to Node.js
* **Infrastructure**: AWS Lambda + API Gateway + Terraform
* **Build tools**: [`shadow-cljs`](https://github.com/thheller/shadow-cljs) for ClojureScript compilation
* **Dependencies**:
  - Server-side rendering with plain HTML generation
  - Markdown parsing with `marked` library
  - Syntax highlighting with `highlight.js`

## Architecture

The blog is now a **server-side rendered application** running on AWS Lambda, replacing the previous SPA architecture. This provides:

- **Better SEO**: Search engines can crawl the fully rendered HTML
- **Faster initial page loads**: No client-side JavaScript required
- **Simplified deployment**: Single Lambda function handles all routes
- **Cost-effective**: Pay-per-request serverless model

## Directory Structure

```
├── src/brotherus/blog/
│   ├── lambda.cljs          # Main Lambda handler
│   ├── server_render.cljs   # Server-side rendering functions
│   ├── filters.cljs        # Article filtering logic
│   └── ...
├── infrastructure/
│   └── main.tf             # Terraform configuration
├── resources/public/
│   └── images/             # Static images
├── target/lambda/          # Compiled Lambda function
├── deploy-lambda.ps1       # Main deployment script
├── build-lambda.ps1        # Build script
└── shadow-cljs.edn        # ClojureScript build configuration
```


## Prerequisites

1. **JDK 8 or later** - Required for ClojureScript compilation
2. **Node.js** - Required for npm dependencies and Lambda runtime
3. **AWS CLI** - Required for deployment to AWS
4. **Terraform** - Required for infrastructure management

## Development

### Local Development

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Build the Lambda function:**
   ```bash
   npm run release-lambda
   # or for development with watch mode:
   npm run watch-lambda
   ```

3. **Test locally:**
   ```bash
   node test-lambda.js
   # or test specific routes:
   node test-lambda.js --filter=post
   ```

### Deployment

1. **Configure AWS credentials:**
   ```bash
   aws configure
   ```

2. **Deploy infrastructure (first time only):**
   ```bash
   cd infrastructure
   terraform init
   terraform apply -var="environment=prod" -var="aws_region=eu-north-1"
   ```

3. **Deploy Lambda function:**
   ```bash
   .\deploy-lambda.ps1
   # or skip infrastructure updates:
   .\deploy-lambda.ps1 -SkipInfra
   ```

4. **Test deployment:**
   ```bash
   # Test specific routes
   node test-lambda.js --filter=page
   ```

## Live Blog

The blog is deployed my deploy-lambda.ps1 script (using Terraform main.tf) to create resources 
up to and including the API Gateway. This makes the app available at:
**https://dlsqhsyaah.execute-api.eu-north-1.amazonaws.com/prod/**

On top of that we have manually created CloudFront distribution and Route53 alias record
mapping to https://www.brotherus.net
In addition to providing nice URL, CloudFront provides caching and compression.
Without caching, the lambda can be slow with long articles, up to 18 sec for Infia article!
But CloudFront cache returns even that in ~300ms (and ~20 ms if in local cache).
 X-Cache response header tells if CloudFront cache was used.
CachePolicy default ttl is 24 hours.
Cloudfront item can be invalidated at: https://us-east-1.console.aws.amazon.com/cloudfront/v4/home?region=eu-north-1#/distributions/E13505H1AVUV02/invalidations/create


## Key Features

- **Server-side rendering** for better SEO and performance
- **Markdown support** with syntax highlighting
- **Responsive design** with clean, readable layout
- **Tag-based filtering** for organizing posts
- **Serverless architecture** for cost-effective hosting

For detailed deployment information, see [README-LAMBDA.md](README-LAMBDA.md).


