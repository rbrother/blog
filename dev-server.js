const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 3003;
const LAMBDA_PATH = './target/lambda/index.js';

// Cache for the lambda handler to enable hot reloading
let cachedHandler = null;
let lastModified = null;

function loadHandler() {
  try {
    // Check if the lambda file exists and get its modification time
    const stats = fs.statSync(LAMBDA_PATH);
    const currentModified = stats.mtime.getTime();
    
    // If file hasn't changed, return cached handler
    if (cachedHandler && lastModified === currentModified) {
      return cachedHandler;
    }
    
    // Clear require cache to enable hot reloading
    const absolutePath = path.resolve(LAMBDA_PATH);
    delete require.cache[absolutePath];
    
    // Load the new handler
    const lambdaModule = require(absolutePath);
    cachedHandler = lambdaModule.handler;
    lastModified = currentModified;
    
    console.log(`✅ Lambda handler loaded/reloaded at ${new Date().toLocaleTimeString()}`);
    return cachedHandler;
  } catch (error) {
    console.error('❌ Error loading lambda handler:', error.message);
    return null;
  }
}

function createLambdaEvent(req) {
  const url = new URL(req.url, `http://${req.headers.host}`);
  
  return {
    // API Gateway v2 format (HTTP API)
    rawPath: url.pathname,
    rawQueryString: url.search.slice(1),
    headers: req.headers,
    requestContext: {
      http: {
        method: req.method,
        path: url.pathname,
        protocol: 'HTTP/1.1',
        sourceIp: req.connection.remoteAddress,
        userAgent: req.headers['user-agent']
      }
    },
    // Also include v1 format for compatibility
    path: url.pathname,
    httpMethod: req.method,
    queryStringParameters: Object.fromEntries(url.searchParams),
    body: null,
    isBase64Encoded: false
  };
}

function createLambdaContext() {
  return {
    callbackWaitsForEmptyEventLoop: false,
    functionName: 'dev-server',
    functionVersion: '$LATEST',
    invokedFunctionArn: 'arn:aws:lambda:local:123456789012:function:dev-server',
    memoryLimitInMB: '512',
    awsRequestId: 'dev-' + Math.random().toString(36).substr(2, 9),
    logGroupName: '/aws/lambda/dev-server',
    logStreamName: new Date().toISOString().replace(/[:.]/g, '-'),
    getRemainingTimeInMillis: () => 30000
  };
}

const server = http.createServer(async (req, res) => {
  console.log(`${req.method} ${req.url}`);
  
  // Load/reload the handler
  const handler = loadHandler();
  if (!handler) {
    res.writeHead(500, { 'Content-Type': 'text/html' });
    res.end(`
      <!DOCTYPE html>
      <html>
        <head><title>Dev Server Error</title></head>
        <body>
          <h1>Lambda Handler Not Available</h1>
          <p>Make sure the lambda build is running:</p>
          <pre>npm run watch-lambda</pre>
          <p>And that the compiled file exists at: <code>${LAMBDA_PATH}</code></p>
          <p><a href="javascript:location.reload()">Reload</a></p>
        </body>
      </html>
    `);
    return;
  }
  
  // Create Lambda event and context
  const event = createLambdaEvent(req);
  const context = createLambdaContext();
  
  try {
    // Call the Lambda handler
    const result = await new Promise((resolve, reject) => {
      handler(event, context, (error, result) => {
        if (error) reject(error);
        else resolve(result);
      });
    });
    
    // Send the response
    res.writeHead(result.statusCode || 200, result.headers || {});
    res.end(result.body || '');
    
  } catch (error) {
    console.error('❌ Lambda handler error:', error);
    res.writeHead(500, { 'Content-Type': 'text/html' });
    res.end(`
      <!DOCTYPE html>
      <html>
        <head><title>Lambda Error</title></head>
        <body>
          <h1>Lambda Handler Error</h1>
          <pre>${error.stack}</pre>
          <p><a href="javascript:location.reload()">Reload</a></p>
        </body>
      </html>
    `);
  }
});

server.listen(PORT, () => {
  console.log(`🚀 Development server running at http://localhost:${PORT}`);
  console.log(`📁 Watching lambda build at: ${LAMBDA_PATH}`);
  console.log('');
  console.log('Make sure to run in another terminal:');
  console.log('  npm run watch-lambda');
  console.log('');
  console.log('Available routes:');
  console.log(`  http://localhost:${PORT}/           - Home page`);
  console.log(`  http://localhost:${PORT}/about      - About page`);
  console.log(`  http://localhost:${PORT}/posts      - All posts`);
  console.log(`  http://localhost:${PORT}/post/...   - Individual posts`);
  console.log('');
});

// Graceful shutdown
process.on('SIGINT', () => {
  console.log('\n👋 Shutting down development server...');
  server.close(() => {
    process.exit(0);
  });
});
