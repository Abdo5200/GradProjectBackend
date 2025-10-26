# AWS S3 Image Upload Setup

This guide will help you set up AWS S3 for image uploads in your Spring Boot application.

## Required AWS Credentials

You need to provide the following credentials in your `application.properties` file:

### 1. AWS Access Key ID

- **Property**: `aws.accessKey`
- **How to get**:
  - Go to AWS Console → IAM → Users → Your User → Security credentials
  - Click "Create access key"
  - Choose "Application running outside AWS" or "EC2"
  - Copy the Access Key ID

### 2. AWS Secret Access Key

- **Property**: `aws.secretKey`
- **How to get**:
  - Created along with the Access Key
  - **Important**: Copy this immediately as it's shown only once

### 3. AWS Region

- **Property**: `aws.region`
- **Examples**:
  - `us-east-1` (N. Virginia)
  - `us-west-2` (Oregon)
  - `eu-west-1` (Ireland)
  - `ap-southeast-1` (Singapore)
- **Where to find**: Look at the top right of your AWS Console

### 4. S3 Bucket Name

- **Property**: `aws.s3.bucketName`
- **How to create**:
  1. Go to AWS Console → S3
  2. Click "Create bucket"
  3. Enter a unique bucket name (e.g., `my-app-images-bucket`)
  4. Choose a region (match your `aws.region`)
  5. Leave defaults for now
  6. Click "Create bucket"

## Configuration in application.properties

Update your `application.properties` file with these values:

```properties
# AWS S3 Configuration
aws.accessKey=YOUR_AWS_ACCESS_KEY_HERE
aws.secretKey=YOUR_AWS_SECRET_KEY_HERE
aws.region=us-east-1
aws.s3.bucketName=your-bucket-name
```

## Bucket Permissions Setup

After creating your bucket, you need to configure permissions:

### Option 1: Public Read Access (For publicly accessible images)

1. Go to your S3 bucket → Permissions tab
2. Edit "Block public access" → Uncheck all boxes
3. Click "Save"
4. Add this bucket policy (replace `your-bucket-name`):

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::your-bucket-name/*"
    }
  ]
}
```

### Option 2: Private Bucket (Secure but images need signed URLs)

- Keep bucket private
- No additional permissions needed
- Images will only be accessible to your application

## IAM User Permissions

Your AWS user needs these permissions:

1. Go to IAM → Users → Your User → Add permissions
2. Choose "Attach policies directly"
3. Select or create a policy with these permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::your-bucket-name",
        "arn:aws:s3:::your-bucket-name/*"
      ]
    }
  ]
}
```

Or simply attach the AWS managed policy: `AmazonS3FullAccess` (for testing purposes)

## API Endpoints

After setup, you can use these endpoints:

### Upload Single Image

```
POST /api/files/upload
Content-Type: multipart/form-data

Parameters:
- file: (required) The image file
- folder: (optional) Folder path in S3, default: "images/"

Example Response:
{
  "url": "https://your-bucket.s3.region.amazonaws.com/images/uuid.jpg",
  "message": "File uploaded successfully"
}
```

### Upload Multiple Images

```
POST /api/files/upload/multiple
Content-Type: multipart/form-data

Parameters:
- files: (required) Array of image files
- folder: (optional) Folder path in S3

Example Response:
{
  "urls": ["url1", "url2", ...],
  "count": 2,
  "message": "Files uploaded successfully"
}
```

### Delete Image

```
DELETE /api/files/delete?url=<s3-url>

Example Response:
{
  "message": "File deleted successfully"
}
```

## Testing with cURL

### Upload single image:

```bash
curl -X POST http://localhost:3000/api/files/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/your/image.jpg" \
  -F "folder=images/"
```

### Upload multiple images:

```bash
curl -X POST http://localhost:3000/api/files/upload/multiple \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "files=@/path/to/image1.jpg" \
  -F "files=@/path/to/image2.jpg"
```

## Security Notes

1. **Never commit credentials to git**: Add `application.properties` to `.gitignore` or use environment variables
2. **Use environment variables** for production:
   ```bash
   export AWS_ACCESS_KEY_ID=your-key
   export AWS_SECRET_ACCESS_KEY=your-secret
   ```
3. **Rotate keys regularly** for security
4. **Limit IAM permissions** to only what's needed (principle of least privilege)

## Troubleshooting

### Error: "Access Denied"

- Check IAM user has S3 permissions
- Verify bucket name is correct
- Ensure bucket and IAM user are in the same region

### Error: "Bucket does not exist"

- Verify bucket name matches exactly (case-sensitive)
- Check that bucket exists in the specified region

### Error: "Invalid credentials"

- Verify access key and secret key are correct
- Check for extra spaces in application.properties
- Ensure credentials are not expired

## Cost Considerations

- S3 storage: ~$0.023 per GB/month
- PUT requests: ~$0.005 per 1,000 requests
- GET requests: ~$0.0004 per 1,000 requests
- First 5 GB are often included in AWS Free Tier
