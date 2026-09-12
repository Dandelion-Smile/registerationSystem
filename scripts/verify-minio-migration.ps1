$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($env:MINIO_VENDOR)) {
    $env:MINIO_VENDOR = 'MinIO'
}

if ([string]::IsNullOrWhiteSpace($env:MINIO_ENDPOINT)) {
    $env:MINIO_ENDPOINT = 'http://117.72.113.94:9000/'
}

if ([string]::IsNullOrWhiteSpace($env:MINIO_ACCESS_KEY)) {
    $env:MINIO_ACCESS_KEY = 'minioadmin'
}

if ([string]::IsNullOrWhiteSpace($env:MINIO_SECRET_KEY)) {
    $env:MINIO_SECRET_KEY = 'Minio@123456'
}

if ([string]::IsNullOrWhiteSpace($env:MINIO_BUCKET_NAME)) {
    $env:MINIO_BUCKET_NAME = 'compserve'
}

if ([string]::IsNullOrWhiteSpace($env:MINIO_REGION)) {
    $env:MINIO_REGION = 'us-east-1'
}

if ([string]::IsNullOrWhiteSpace($env:MINIO_USE_HTTPS)) {
    $env:MINIO_USE_HTTPS = 'false'
}

mvn -pl compserve-common -am -Dtest=MinioMigrationIntegrationTest test
