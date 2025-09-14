const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');

// Directories to watch for changes
const watchDirs = ['src'];
const debounceMs = 1000; // Wait 1 second after last change before rebuilding

let buildTimeout = null;
let isBuilding = false;

function buildLambda() {
  if (isBuilding) {
    console.log('⏳ Build already in progress, skipping...');
    return;
  }
  
  isBuilding = true;
  console.log('🔨 Building lambda...');
  
  const buildProcess = spawn('npx', ['shadow-cljs', 'release', 'lambda'], {
    stdio: 'inherit',
    shell: true
  });
  
  buildProcess.on('close', (code) => {
    isBuilding = false;
    if (code === 0) {
      console.log('✅ Lambda build completed successfully!');
    } else {
      console.log('❌ Lambda build failed with code:', code);
    }
  });
  
  buildProcess.on('error', (error) => {
    isBuilding = false;
    console.error('❌ Build process error:', error);
  });
}

function scheduleRebuild() {
  if (buildTimeout) {
    clearTimeout(buildTimeout);
  }
  
  buildTimeout = setTimeout(() => {
    buildLambda();
  }, debounceMs);
}

function watchDirectory(dir) {
  try {
    fs.watch(dir, { recursive: true }, (eventType, filename) => {
      if (filename && (filename.endsWith('.cljs') || filename.endsWith('.clj'))) {
        console.log(`📝 File changed: ${filename}`);
        scheduleRebuild();
      }
    });
    console.log(`👀 Watching directory: ${dir}`);
  } catch (error) {
    console.error(`❌ Error watching directory ${dir}:`, error);
  }
}

console.log('🚀 Starting file watcher for lambda development...');
console.log('This will rebuild the lambda in release mode when source files change.');
console.log('');

// Initial build
buildLambda();

// Start watching directories
watchDirs.forEach(watchDirectory);

console.log('');
console.log('Press Ctrl+C to stop watching...');

// Graceful shutdown
process.on('SIGINT', () => {
  console.log('\n👋 Stopping file watcher...');
  process.exit(0);
});
