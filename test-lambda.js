const { handler } = require('./target/lambda/index.js');

// Parse command line arguments
const args = process.argv.slice(2);
const filterArg = args.find(arg => arg.startsWith('--filter='));
const testFilter = filterArg ? filterArg.split('=')[1] : null;

// Test different routes
const testRoutes = [
  { path: '/', description: 'Home page', expectedStatus: 200 },
  { path: '/about', description: 'About page', expectedStatus: 200 },
  { path: '/posts', description: 'All posts', expectedStatus: 200 },
  { path: '/posts/tag/clojure', description: 'Posts tagged with clojure', expectedStatus: 200 },
  { path: '/post/blog-tech-stack', description: 'Tech stack post (should exist)', expectedStatus: 200 },
  { path: '/post/airbnb-mantyharju-instructions', description: 'Mäntyharju instructions (should exist)', expectedStatus: 200 },
  { path: '/post/nonexistent-article', description: 'Non-existent post', expectedStatus: 200 },
  { path: '/nonexistent', description: 'Non-existent page', expectedStatus: 200 }
];

async function testLambda() {
  // Show help if requested
  if (args.includes('--help') || args.includes('-h')) {
    console.log('Usage: node test-lambda.js [options]');
    console.log('Options:');
    console.log('  --filter=<text>    Run only tests whose description contains <text>');
    console.log('  --help, -h         Show this help message');
    console.log('\nExamples:');
    console.log('  node test-lambda.js                    # Run all tests');
    console.log('  node test-lambda.js --filter=post      # Run only tests with "post" in description');
    console.log('  node test-lambda.js --filter=404       # Run only tests with "404" in description');
    return;
  }

  // Filter tests based on command line argument
  const filteredRoutes = testFilter
    ? testRoutes.filter(route => route.description.toLowerCase().includes(testFilter.toLowerCase()))
    : testRoutes;

  if (filteredRoutes.length === 0) {
    console.log(`No tests found matching filter: "${testFilter}"`);
    console.log('Available test descriptions:');
    testRoutes.forEach(route => console.log(`  - ${route.description}`));
    return;
  }

  console.log('Testing Lambda function...');
  if (testFilter) {
    console.log(`Filter: "${testFilter}" (${filteredRoutes.length}/${testRoutes.length} tests)`);
  }
  console.log('\n');

  let passedTests = 0;
  let totalTests = filteredRoutes.length;

  for (const route of filteredRoutes) {
    console.log(`Testing: ${route.description} (${route.path})`);
    console.log(`Expected status: ${route.expectedStatus}`);
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
      
      console.log('Actual status:', result.statusCode);

      // Verify status code
      const statusMatch = result.statusCode === route.expectedStatus;
      if (statusMatch) {
        console.log('✅ Status code matches expected');
        passedTests++;
      } else {
        console.log(`❌ Status code mismatch! Expected: ${route.expectedStatus}, Got: ${result.statusCode}`);
      }

      // Show content preview
      const bodyMatch = result.body.match(/<body[^>]*>([\s\S]*?)<\/body>/);
      const bodyContent = bodyMatch ? bodyMatch[1].trim() : result.body;
      console.log('Body:', bodyContent);
      console.log('\n');
      
    } catch (error) {
      console.error('❌ Error:', error);
      console.log('\n');
    }
  }

  // Print test summary
  console.log('=' .repeat(60));
  console.log('TEST SUMMARY');
  console.log('=' .repeat(60));
  console.log(`Passed: ${passedTests}/${totalTests}`);
  console.log(`Success rate: ${((passedTests / totalTests) * 100).toFixed(1)}%`);

  if (passedTests === totalTests) {
    console.log('🎉 All tests passed!');
  } else {
    console.log('⚠️  Some tests failed. Please check the results above.');
  }
}

testLambda().catch(console.error);
