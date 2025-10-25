import axiosInstance from "./axiosInstance";

export interface FileItem {
  id: number;
  fileName: string;
  fileType: string;
  fileSize: number;
  uploadDate: string;
  fileUrl: string;
}

export interface UploadResponse {
  message: string;
  fileUrl: string;
  fileName: string;
  fileSize: number;
}

export interface UploadError {
  message: string;
  status: number;
}

export class FileUploadService {
  async uploadFile(file: File): Promise<UploadResponse> {
    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await axiosInstance.post("/files/upload", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      return response.data;
    } catch (error: any) {
      throw {
        message: error.response?.data?.message || "Upload failed",
        status: error.response?.status || 500,
      };
    }
  }

  async getAllFiles(): Promise<FileItem[]> {
    try {
      const response = await axiosInstance.get("/files/all");
      return response.data;
    } catch (error: any) {
      throw {
        message: error.response?.data?.message || "Failed to fetch files",
        status: error.response?.status || 500,
      };
    }
  }

  isValidFileType(file: File): boolean {
    const allowedTypes = [
      "application/pdf",
      "video/mp4",
      "image/jpeg",
      "image/jpg",
      "image/png",
    ];
    return allowedTypes.includes(file.type);
  }

  getFileType(file: File): string {
    if (file.type === "application/pdf") return "PDF";
    if (file.type === "video/mp4") return "Video";
    if (file.type.includes("image")) return "Image";
    return "Unknown";
  }

  getFileIcon(fileType: string): string {
    if (fileType === "application/pdf") return "📄";
    if (fileType === "video/mp4") return "🎥";
    if (fileType.includes("image")) return "🖼️";
    return "📎";
  }

  // Format file size
  formatFileSize(bytes: number): string {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  }

  // Format date
  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  getAcceptedFileExtensions(): string {
    return ".pdf,.mp4,.jpg,.jpeg,.png";
  }
}

export const fileUploadService = new FileUploadService();
