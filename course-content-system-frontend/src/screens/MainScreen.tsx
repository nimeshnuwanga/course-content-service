import { FilesList } from "@/components/content/FilesList";
import { UploadFileDialog } from "@/components/content/UploadFileDialog";
import { useState } from "react";

const MainScreen = () => {
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const handleUploadSuccess = (response: any) => {
    console.log("File uploaded successfully:", response);
    setRefreshTrigger((prev) => prev + 1);
  };
  return (
    <div className="min-h-screen bg-background">
      <div className="container max-w-7xl mx-auto px-4 py-6">
        {/* Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h4 className="scroll-m-20 text-2xl font-semibold tracking-tight">
              Course Content Manager
            </h4>
            <p className="text-muted-foreground mt-1">
              Upload and manage your contents in one place
            </p>
          </div>
          <UploadFileDialog onUploadSuccess={handleUploadSuccess} />
        </div>

        {/* Files List */}
        <FilesList refreshTrigger={refreshTrigger} />
      </div>
    </div>
  );
};

export default MainScreen;
