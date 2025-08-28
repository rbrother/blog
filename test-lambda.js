const { handler } = require('./target/lambda/index.js');

// Test different routes
const testRoutes = [
  { path: '/', description: 'Home page' },
  { path: '/about', description: 'About page' },
  { path: '/posts', description: 'All posts' },
  { path: '/posts/tag/clojure', description: 'Posts tagged with clojure' },
  { path: '/post/1', description: 'Specific post' },
  { path: '/nonexistent', description: 'Non-existent page (should be 404)' }
];

async function testLambda() {
  console.log('Testing Lambda function...\n');
  
  for (const route of testRoutes) {
    console.log(`Testing: ${route.description} (${route.path})`);
    console.log('=' .repeat(50));
    
    const event = {
      "path": route.path,
      "httpMethod": "GET",
      "headers": {},
      "queryStringParameters": null,
      "body": null
    };
    
    const context = {};
    
    try {
      const result = await new Promise((resolve, reject) => {
        handler(event, context, (error, result) => {
          if (error) reject(error);
          else resolve(result);
        });
      });
      
      console.log('Status:', result.statusCode);
      console.log('Headers:', JSON.stringify(result.headers, null, 2));
      console.log('Body preview:', result.body.substring(0, 200) + '...');
      console.log('\n');
      
    } catch (error) {
      console.error('Error:', error);
      console.log('\n');
    }
  }
}

testLambda().catch(console.error);
