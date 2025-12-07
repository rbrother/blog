# Lambda Performance Analysis & Optimization Plan

## Current Performance Issues

### Measured Times:
- **Local dev server**: 3.2 seconds for /post/infia
- **AWS Lambda (warm)**: 18-27 seconds for /post/infia  
- **AWS Lambda (cold)**: Can be even slower due to cold start

### Test Results:
```
Homepage (cold start): 3.5s
Homepage (warm): 328-492ms ✅
Article /post/infia (warm): 18-27s ❌
```

## Root Causes Identified

### 1. **Markdown Processing is CPU-Intensive** (PRIMARY ISSUE)
The markdown-to-hiccup conversion using:
- `marked` library for parsing
- `marked-highlight` for syntax highlighting  
- `highlight.js` with 7 language parsers loaded
- Custom hiccup transformations with specter

This is happening on EVERY request for article pages, even when content hasn't changed.

### 2. **Lambda CPU is Limited at 512MB**
Lambda CPU scales with memory:
- 512MB = ~0.29 vCPU
- 1024MB = ~0.58 vCPU  
- 1769MB = 1 vCPU
- 10240MB = 6 vCPUs

### 3. **No Content Caching**
Article content is fetched and processed fresh every time, even though:
- Articles rarely change
- Markdown processing result is deterministic
- Could be cached after first render

## Optimization Solutions

### Quick Wins (Implement First)

#### 1. **Increase Lambda Memory** (EASIEST - DO THIS FIRST)
**Cost**: Minimal (you pay per GB-second, but faster = less seconds)
**Benefit**: 2-4x CPU performance

```terraform
# In infrastructure/main.tf
resource "aws_lambda_function" "blog_lambda" {
  memory_size = 1769  # Change from 512 to 1769 (1 full vCPU)
  # OR even 2048 for more headroom
}
```

**Expected improvement**: 18s → 6-9s

#### 2. **Add Rendered Content Caching**
Cache the rendered hiccup/HTML after markdown processing.

```clojure
;; In lambda.cljs or article.cljs
(def rendered-cache (atom {}))

(defn get-or-render-article [article-id markdown]
  (if-let [cached (get @rendered-cache article-id)]
    (js/Promise.resolve cached)
    (let [rendered (article/markdown-to-hiccup markdown {:item-id article-id})]
      (swap! rendered-cache assoc article-id rendered)
      (js/Promise.resolve rendered))))
```

**Expected improvement**: Second request: 18s → 50-100ms

#### 3. **Enable Lambda SnapStart** (Node.js 18+)
Pre-initializes Lambda containers for faster cold starts.
Not available for Node.js yet - but worth monitoring.

### Medium-Term Solutions

#### 4. **Pre-render Articles at Build Time**
Generate static HTML for articles during deployment:

```bash
# In deploy-lambda.sh, after building:
npm run prerender-articles
```

Store pre-rendered HTML in S3 or bundle with Lambda.

**Expected improvement**: 18s → 200-500ms

#### 5. **Use DynamoDB for Rendered Cache**
Persist cached renders across Lambda instances:

```clojure
(defn get-cached-from-dynamodb [article-id]
  ;; Fetch pre-rendered HTML from DynamoDB
  )
```

**Cost**: ~$0.25/month for occasional updates
**Benefit**: All Lambda instances share cache

#### 6. **Optimize Markdown Parser**
- Lazy-load highlight.js languages (only load on demand)
- Use faster markdown parser (marked is relatively slow)
- Consider using marked's async API

### Long-Term Solutions

#### 7. **Hybrid Static + Lambda**
- Serve articles from S3/CloudFront (static)
- Use Lambda only for dynamic features (view counter)
- Much cheaper and faster

#### 8. **Move to Faster Runtime**
- Node.js is slower than Go/Rust for CPU-intensive tasks
- Could compile critical paths to WASM

## Recommended Immediate Action Plan

### Step 1: Increase Lambda Memory (5 minutes)
```bash
# Update Terraform
cd infrastructure
# Edit main.tf to change memory_size from 512 to 1769
terraform apply -var="aws_region=eu-north-1"
```

### Step 2: Add Simple Caching (15 minutes)
Add rendered content cache in lambda.cljs

### Step 3: Test
Should see: 18s → 6-7s first request, <100ms subsequent requests

### Step 4: Monitor
Check CloudWatch metrics for:
- Duration
- Memory Used
- Cost impact

## Cost Analysis

### Current: 512MB Lambda
- Duration: 18s
- Cost: 18s * 512MB = 9,216 MB-seconds
- Price: ~$0.153 per 1M requests

### Proposed: 1769MB Lambda  
- Duration: 6s (estimated)
- Cost: 6s * 1769MB = 10,614 MB-seconds
- Price: ~$0.177 per 1M requests

**Cost increase**: ~16% for 3x performance improvement ✅

With caching:
- First request: 6s
- Subsequent: 50ms
- Average with 10 views per article: ~550ms
- Effective cost: Much lower!

## Implementation Priority

1. ✅ **Increase Lambda memory to 1769MB** - Do this NOW (5 min)
2. ⭐ **Add rendered content caching** - Quick win (15 min)  
3. 📊 **Monitor and measure** - Verify improvements
4. 🔄 **Consider pre-rendering** - If still too slow

## Expected Final Performance

After all quick wins:
- **First view (cache miss)**: 5-7 seconds
- **Subsequent views (cache hit)**: 50-200ms
- **Cold start overhead**: 400-600ms (reduced with more memory)

This brings Lambda performance much closer to your local dev experience!

