# Course Content Management System

A simple file upload and management system built with Spring Boot and React.

## Prerequisites

- Java 21+
- Node.js 18+
- MySQL 8.0+
- Maven 3.9+

## Database Setup

```sql
CREATE DATABASE ccus;

USE ccus;

CREATE TABLE course_content (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_url TEXT NOT NULL
);
```

## Backend Setup

```bash
cd course-content-system-backend

# Configure database (optional - uses defaults)
export DB_URL=jdbc:mysql://localhost:3306/ccus
export DB_USERNAME=root
export DB_PASSWORD=your_password

# Run application
./mvnw spring-boot:run
```

Backend runs on: `http://localhost:8080`

## Frontend Setup

```bash
cd course-content-system-frontend

# Install dependencies
npm install

# Run development server
npm run dev
```

Frontend runs on: `http://localhost:5173`

## Configuration

**Backend** (`application.properties`):
- Max file size: 50MB
- Allowed types: PDF, MP4, JPG, JPEG, PNG
- Upload directory: `./uploads`

**Database defaults**:
- Host: `localhost:3306`
- Database: `ccus`
- Username: `root`
- Password: `password`

## API Endpoints

- `POST /api/files/upload` - Upload file
- `GET /api/files/all` - List all files
- `GET /api/files/{id}` - Get file details
- `GET /api/files/download/{fileName}` - Download file
- `DELETE /api/files/{id}` - Delete file

## License

MIT License - See LICENSE file for details
