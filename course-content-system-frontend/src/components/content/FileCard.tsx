import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import type { FileItem } from "@/services/fileUploadService";

interface FileCardProps {
  file: FileItem;
  onDownload?: (file: FileItem) => void;
}

export function FileCard({ file, onDownload }: FileCardProps) {
  const fileUploadService = {
    getFileIcon: (fileType: string) => {
      if (fileType === "application/pdf") return "📄";
      if (fileType === "video/mp4") return "🎥";
      if (fileType.includes("image")) return "🖼️";
      return "📎";
    },
    getFileType: (fileType: string) => {
      if (fileType === "application/pdf") return "PDF";
      if (fileType === "video/mp4") return "Video";
      if (fileType.includes("image")) return "Image";
      return "Unknown";
    },
    formatFileSize: (bytes: number) => {
      if (bytes === 0) return "0 Bytes";
      const k = 1024;
      const sizes = ["Bytes", "KB", "MB", "GB"];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
    },
    formatDate: (dateString: string) => {
      return new Date(dateString).toLocaleDateString("en-US", {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    },
  };

  const handleDownload = () => {
    if (onDownload) {
      onDownload(file);
    } else {
      // Default download behavior
      window.open(file.fileUrl, "_blank");
    }
  };

  const getVariant = (fileType: string) => {
    if (fileType === "application/pdf") return "destructive";
    if (fileType === "video/mp4") return "default";
    if (fileType.includes("image")) return "secondary";
    return "outline";
  };

  return (
    <Card className="hover:shadow-lg transition-all duration-300">
      <CardHeader className="pb-3">
        <div className="flex justify-between items-start mb-2">
          <div className="flex items-center gap-2">
            <span className="text-2xl">
              {fileUploadService.getFileIcon(file.fileType)}
            </span>
            <CardTitle className="text-lg leading-6">{file.fileName}</CardTitle>
          </div>
          <Badge variant={getVariant(file.fileType) as any}>
            {fileUploadService.getFileType(file.fileType)}
          </Badge>
        </div>
        <CardDescription className="line-clamp-2">
          Uploaded on {fileUploadService.formatDate(file.uploadDate)}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="flex justify-between items-center text-sm text-muted-foreground mb-4">
          <span>Size: {fileUploadService.formatFileSize(file.fileSize)}</span>
          <span>ID: #{file.id}</span>
        </div>
        <Button onClick={handleDownload} className="w-full" variant="outline">
          Download File
        </Button>
      </CardContent>
    </Card>
  );
}
