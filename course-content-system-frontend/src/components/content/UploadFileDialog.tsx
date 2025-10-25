import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { fileUploadService } from "@/services";

interface UploadFileDialogProps {
  onUploadSuccess?: (response: any) => void;
}

export function UploadFileDialog({ onUploadSuccess }: UploadFileDialogProps) {
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [file, setFile] = useState<File | null>(null);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = event.target.files?.[0] || null;

    if (selectedFile) {
      // Validate file type
      if (fileUploadService.isValidFileType(selectedFile)) {
        setFile(selectedFile);
        setError(null);
      } else {
        setError(
          "Invalid file type. Please select PDF, MP4, JPG, JPEG, or PNG files."
        );
        setFile(null);
      }
    }
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    if (!file) {
      setError("Please select a file");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await fileUploadService.uploadFile(file);

      // Reset form
      setFile(null);
      setOpen(false);

      // Notify parent component
      if (onUploadSuccess) {
        onUploadSuccess(response);
      }

      console.log("Upload successful:", response);
    } catch (err: any) {
      setError(err.message || "Failed to upload file");
    } finally {
      setLoading(false);
    }
  };

  const handleOpenChange = (isOpen: boolean) => {
    if (!isOpen) {
      // Reset form when dialog closes
      setFile(null);
      setError(null);
    }
    setOpen(isOpen);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <Button>Upload File</Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>Upload File</DialogTitle>
            <DialogDescription>
              Upload PDF, MP4, JPG, JPEG, or PNG files.
            </DialogDescription>
          </DialogHeader>

          <div className="grid gap-4 py-4">
            {/* File Upload */}
            <div className="grid gap-2">
              <Label htmlFor="file">Select File</Label>
              <Input
                id="file"
                type="file"
                onChange={handleFileChange}
                accept={fileUploadService.getAcceptedFileExtensions()}
                required
              />
              <p className="text-sm text-muted-foreground">
                Supported formats: PDF, MP4, JPG, JPEG, PNG
              </p>

              {/* File Info Display */}
              {file && (
                <div className="p-3 border rounded-md bg-muted/50">
                  <p className="text-sm font-medium">{file.name}</p>
                  <p className="text-xs text-muted-foreground">
                    Type: {fileUploadService.getFileType(file)} • Size:{" "}
                    {(file.size / (1024 * 1024)).toFixed(2)} MB
                  </p>
                </div>
              )}
            </div>

            {/* Error Display */}
            {error && (
              <div className="p-3 text-sm text-red-600 bg-red-50 rounded-md border border-red-200">
                {error}
              </div>
            )}
          </div>

          <DialogFooter>
            <DialogClose asChild>
              <Button type="button" variant="outline" disabled={loading}>
                Cancel
              </Button>
            </DialogClose>
            <Button type="submit" disabled={loading || !file}>
              {loading ? "Uploading..." : "Upload File"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
