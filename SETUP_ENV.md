# Environment Variables Setup Guide

This application uses environment variables to protect sensitive information like database credentials, API keys, and JWT secrets.

## Quick Setup

### For Local Development

1. **Copy the example file:**

   ```bash
   cp secrets.properties.example secrets.properties
   ```

2. **Edit `secrets.properties`** with your actual credentials

3. **Load environment variables** before running the application.

### Loading Environment Variables

#### Option 1: PowerShell (Windows)

```powershell
# Load environment variables from secrets.properties
Get-Content secrets.properties | ForEach-Object {
    if ($_ -match '^([^=]+)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
    }
}
```

#### Option 2: Using a .env file (Recommended)

If you're using an IDE like IntelliJ IDEA or VS Code with extensions, you can create a `.env` file:

```bash
# .env file
DATABASE_URL=jdbc:mysql://localhost:3306/todolist
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password
# ... other variables
```

#### Option 3: Manual Export (Linux/Mac)

```bash
export $(grep -v '^#' secrets.properties | xargs)
```

#### Option 4: IntelliJ IDEA

1. Go to Run → Edit Configurations
2. Select your Spring Boot configuration
3. Under "Environment variables", click the folder icon
4. Add variables from `secrets.properties`

#### Option 5: VS Code

Create or edit `.vscode/launch.json`:

```json
{
  "configurations": [
    {
      "type": "java",
      "name": "Spring Boot",
      "request": "launch",
      "mainClass": "com.example.gradproject.GradProjectBackend",
      "envFile": "${workspaceFolder}/secrets.properties"
    }
  ]
}
```

## Required Environment Variables

| Variable             | Description                      | Example                                |
| -------------------- | -------------------------------- | -------------------------------------- |
| `DATABASE_URL`       | MySQL connection URL             | `jdbc:mysql://localhost:3306/todolist` |
| `DATABASE_USERNAME`  | Database username                | `springstudent`                        |
| `DATABASE_PASSWORD`  | Database password                | `your_password`                        |
| `GMAIL_USERNAME`     | Gmail address for sending emails | `your@gmail.com`                       |
| `GMAIL_APP_PASSWORD` | Gmail app-specific password      | `xxxx xxxx xxxx xxxx`                  |
| `FRONTEND_URL`       | Frontend application URL         | `http://localhost:8080`                |
| `FROM_EMAIL`         | Email sender address             | `your@gmail.com`                       |
| `FROM_NAME`          | Email sender name                | `Your App Team`                        |
| `JWT_SECRET`         | Secret key for JWT tokens        | `your-secret-key`                      |
| `JWT_EXPIRATION`     | JWT token expiration (ms)        | `86400000`                             |
| `AWS_ACCESS_KEY`     | AWS access key ID                | `AKIA...`                              |
| `AWS_SECRET_KEY`     | AWS secret access key            | `...`                                  |
| `AWS_REGION`         | AWS region                       | `eu-north-1`                           |
| `AWS_S3_BUCKET_NAME` | S3 bucket name                   | `your-bucket-name`                     |

## Production Deployment

For production environments (AWS, Heroku, etc.), set environment variables in your hosting platform:

### AWS EC2/Elastic Beanstalk

- Use AWS Systems Manager Parameter Store
- Or set in Elastic Beanstalk environment configuration

### Heroku

```bash
heroku config:set DATABASE_URL=your_url
heroku config:set JWT_SECRET=your_secret
# ... etc
```

### Docker

```dockerfile
ENV DATABASE_URL=your_url
ENV JWT_SECRET=your_secret
# ... etc
```

## Security Notes

- ✅ Never commit `secrets.properties` to version control
- ✅ Never commit `.env` files
- ✅ Use different credentials for development and production
- ✅ Rotate secrets regularly
- ✅ Use strong, random values for `JWT_SECRET`
- ✅ Use IAM roles instead of hardcoded AWS credentials when possible

## Troubleshooting

### "Could not resolve placeholder"

- Make sure environment variables are loaded
- Check that variable names match exactly (case-sensitive)

### Email sending fails

- Verify Gmail app password is correct
- Check that 2-factor authentication is enabled in Google account

### AWS S3 access denied

- Verify IAM user has S3 permissions
- Check bucket policies and CORS configuration
