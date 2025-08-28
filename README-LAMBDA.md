# Brotherus Blog - Lambda Backend

This is the AWS Lambda backend version of the Brotherus Blog, converted from a ClojureScript SPA to a server-side rendered application running on AWS Lambda.

## Architecture

The application now consists of:

- **ClojureScript Lambda Function**: Server-side rendering of blog pages
- **AWS API Gateway**: HTTP routing to the Lambda function
- **S3 Bucket**: Static assets (CSS, images)
- **Terraform**: Infrastructure as Code for AWS resources

## Key Changes from SPA

1. **Server-Side Rendering**: Pages are now rendered on the server using ClojureScript
2. **Lambda Handler**: Main entry point that processes HTTP requests
3. **Static Asset Serving**: CSS and images served from S3
4. **Markdown Processing**: Still uses the `marked` library but server-side
5. **No Client-Side JavaScript**: Pure server-rendered HTML

## Prerequisites

- Node.js (v18 or later)
- AWS CLI configured with appropriate credentials
- Terraform (v1.0 or later)
- PowerShell (for Windows deployment scripts)

## Development

### Building the Application

```powershell
# Clean build
.\build.ps1 -Clean

# Development build with watch mode
.\build.ps1 -Watch

# Production build
.\build.ps1
```

### Local Testing

The Lambda function can be tested locally using the AWS SAM CLI or by running Node.js directly:

```bash
# After building
cd target/lambda
node -e "const handler = require('./index.js').handler; handler({path: '/', httpMethod: 'GET'}, {}, console.log)"
```

## Deployment

### Full Deployment (Infrastructure + Code)

```powershell
# Deploy to production
.\deploy-lambda.ps1

# Deploy to staging
.\deploy-lambda.ps1 -Environment staging

# Deploy to specific region
.\deploy-lambda.ps1 -Region us-west-2
```

### Code-Only Deployment

If infrastructure is already deployed:

```powershell
# Update only the Lambda function code
.\deploy-lambda.ps1 -SkipInfra
```

### Infrastructure-Only Deployment

```powershell
# Deploy only infrastructure changes
.\deploy-lambda.ps1 -SkipBuild
```

## Project Structure

```
├── src/brotherus/blog/
│   ├── lambda.cljs          # Main Lambda handler
│   ├── server_render.cljs   # Server-side rendering functions
│   ├── db.cljs             # Article data (unchanged)
│   ├── filters.cljs        # Article filtering (unchanged)
│   └── ...                 # Other existing modules
├── infrastructure/
│   └── main.tf             # Terraform configuration
├── target/lambda/          # Compiled Lambda function
├── deploy-lambda.ps1       # Main deployment script
├── build.ps1              # Build script
└── shadow-cljs.edn        # Updated with Lambda target
```

## Environment Variables

The Lambda function supports these environment variables:

- `NODE_ENV`: Environment (dev, staging, prod)

## Monitoring

- **CloudWatch Logs**: Lambda function logs are available in CloudWatch
- **API Gateway Logs**: Request/response logs for debugging
- **Metrics**: Lambda execution metrics in CloudWatch

## Static Assets

Static assets (CSS, images) are served from S3:

- CSS files: Served from S3 bucket
- Images: Served from S3 bucket
- The Lambda function generates HTML that references these S3 URLs

## Routing

The application handles these routes:

- `/` - Home page with article list
- `/post/:id` - Individual article page
- `/posts/:tag` - Articles filtered by tag
- `/about` - About page

## Article Content

Articles are still fetched from the GitHub repository:
- Base URL: `https://raw.githubusercontent.com/rbrother/articles/refs/heads/main/`
- Markdown files are processed server-side using the `marked` library

## Troubleshooting

### Common Issues

1. **Build Failures**: Ensure all dependencies are installed with `npm install`
2. **Deployment Failures**: Check AWS credentials and permissions
3. **Lambda Timeouts**: Increase timeout in Terraform configuration if needed
4. **Static Asset Issues**: Verify S3 bucket permissions and CORS settings

### Debugging

1. Check CloudWatch logs for Lambda function errors
2. Use API Gateway test console for request debugging
3. Test Lambda function locally before deployment

## Cost Optimization

- Lambda functions are billed per request and execution time
- S3 storage costs are minimal for static assets
- API Gateway costs scale with request volume
- Consider CloudFront CDN for high-traffic scenarios

## Security

- Lambda function has minimal IAM permissions
- S3 bucket allows public read access for static assets only
- API Gateway has CORS configured for web access
- No sensitive data stored in environment variables
