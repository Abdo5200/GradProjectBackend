# Redis Caching Setup for Presigned URLs

## Overview

This project now uses Redis to cache S3 presigned URLs for 59 minutes, preventing redundant AWS API calls when
requesting the same image within that timeframe.

## What Was Implemented

### 1. Dependencies Added (pom.xml)

- `spring-boot-starter-data-redis` - Redis integration
- `spring-boot-starter-cache` - Spring Cache abstraction

### 2. Configuration Files

#### RedisCacheConfig.java

- Configures Redis cache manager
- Sets TTL to 59 minutes for `presignedUrls` cache (slightly less than 60-minute URL expiry)
- Default TTL of 10 minutes for other caches

#### application.properties

```properties
spring.cache.type=redis
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.timeout=60000
```

### 3. Caching Logic

#### S3Service.java

- **@Cacheable** on `generatePresignedUrl()` - caches URLs by S3 key
- **@CacheEvict** on `deleteByKey()` - removes cached URL when file is deleted

### 4. Flow

**Upload Flow:**

1. `uploadFile()` → uploads to S3 and gets key
2. `generatePresignedUrl(key)` → generates URL and caches it (59 min TTL)
3. Returns cached URL

**View Flow:**

1. User requests `/api/files/view?key=images/abc.jpg`
2. `generatePresignedUrl(key)` checks cache first
3. **Cache HIT** → returns cached URL instantly (no AWS call)
4. **Cache MISS** → generates new URL, caches it, returns it

**Get My Photos Flow:**

1. User requests `/api/files/my-images`
2. For each image key, `generatePresignedUrl(key)` is called
3. **Cached keys** → return instantly
4. **New keys** → generate once and cache

**Delete Flow:**

1. `deleteFile()` → calls `deleteByKey()`
2. `@CacheEvict` removes cached URL
3. Deletes file from S3

## Setup Redis

### Option 1: Docker (Recommended)

```bash
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

### Option 2: Linux Native

```bash
sudo apt-get update
sudo apt-get install redis-server
sudo systemctl start redis
sudo systemctl enable redis
```

### Option 3: Remote Redis (Production)

Update `application.properties`:

```properties
spring.data.redis.host=your-redis-host.com
spring.data.redis.port=6379
spring.data.redis.password=your-password
```

Or use environment variables:

```bash
export REDIS_HOST=your-redis-host.com
export REDIS_PORT=6379
export REDIS_PASSWORD=your-password
```

## Testing

### 1. Start Redis

```bash
docker start redis
```

### 2. Build and Run Application

```bash
./mvnw clean install
./mvnw spring-boot:run
```

### 3. Test Caching

**First Request (Cache MISS):**

```bash
curl "http://localhost:3000/api/files/view?key=images/test.jpg"
```

Check logs: `🔄 Generating new presigned URL for key: images/test.jpg`

**Second Request (Cache HIT):**

```bash
curl "http://localhost:3000/api/files/view?key=images/test.jpg"
```

No generation log → returned from cache instantly!

### 4. Monitor Redis (Optional)

```bash
docker exec -it redis redis-cli
> KEYS *
> TTL presignedUrls::images/test.jpg
> GET presignedUrls::images/test.jpg
```

## Benefits

1. **Performance**: No AWS API calls for cached URLs (sub-millisecond response)
2. **Cost**: Reduces AWS S3 API request costs
3. **Scalability**: Distributed cache works across multiple app instances
4. **User Experience**: Faster image loading in `/my-images` endpoint

## Cache Behavior

- **TTL**: 59 minutes (cache expires 1 minute before URL expiry)
- **Eviction**: Automatic on file deletion
- **Storage**: Each URL ~200-500 bytes in Redis
- **Capacity**: Default unlimited (configure in RedisCacheConfig if needed)

## Troubleshooting

### Can't connect to Redis

```
Caused by: io.lettuce.core.RedisConnectionException: Unable to connect
```

**Solution**: Ensure Redis is running on `localhost:6379`

### Cache not working

Check logs for:

- `🔄 Generating new presigned URL` → cache miss
- No log on repeated requests → cache hit

### Clear cache manually

```bash
docker exec -it redis redis-cli FLUSHALL
```

## Environment Variables

| Variable         | Default   | Description              |
|------------------|-----------|--------------------------|
| `REDIS_HOST`     | localhost | Redis server hostname    |
| `REDIS_PORT`     | 6379      | Redis server port        |
| `REDIS_PASSWORD` | (none)    | Redis password if needed |
