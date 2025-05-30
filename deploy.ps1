npm install
Remove-Item -Recurse -Force .\resources\public\js\compiled
npm run release
aws s3 cp .\resources\public  s3://brotherus.net/ --recursive --cache-control "no-cache, no-store, must-revalidate"
# Deploys to: http://brotherus.net.s3-website.eu-north-1.amazonaws.com  -> OK!
# Mapped to Cloudfront disribution: https://d34ov319dta0zj.cloudfront.net  -> OK!
# Mapped in Route 53 to https://brotherus.net or https://www.brotherus.net ->