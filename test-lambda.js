const { handler } = require('./target/lambda/index.js');

// Parse command line arguments
const args = process.argv.slice(2);
const path = args[0];

async function testLambda() {
  // Show help if requested
  if (args.includes('--help') || args.includes('-h')) {
    console.log('\nExamples:');
    console.log('  node test-lambda.js /post/airbnb-mantyharju-activities');
    return;
  }

  console.log('Testing Lambda function...');


    console.log(`Testing: ${path}`);

    const event = {
      "path": path,
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

testLambda().catch(console.error);
