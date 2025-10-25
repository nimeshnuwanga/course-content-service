import axios from "axios";

// Create simple axios instance for file uploads
const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  timeout: 30000, // 30 seconds timeout for file uploads
});

export default axiosInstance;
