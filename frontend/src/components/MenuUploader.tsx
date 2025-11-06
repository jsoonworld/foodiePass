import { useCallback, useState } from 'react';
import { Upload, X, Image as ImageIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { validateImageSize, validateImageType } from '@/utils/imageUtils';

interface MenuUploaderProps {
  onImageSelect: (file: File | null) => void;
  disabled?: boolean;
}

export default function MenuUploader({ onImageSelect, disabled }: MenuUploaderProps) {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isDragging, setIsDragging] = useState(false);

  const handleFile = useCallback((file: File) => {
    setError(null);

    if (!validateImageType(file)) {
      setError('JPG, PNG, HEIC 형식만 지원합니다.');
      return;
    }

    if (!validateImageSize(file)) {
      setError('이미지 크기는 10MB 이하여야 합니다.');
      return;
    }

    setSelectedFile(file);
    onImageSelect(file);

    const reader = new FileReader();
    reader.onload = (e) => {
      setPreview(e.target?.result as string);
    };
    reader.readAsDataURL(file);
  }, [onImageSelect]);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    
    const file = e.dataTransfer.files[0];
    if (file) handleFile(file);
  }, [handleFile]);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  }, []);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  }, []);

  const handleFileInput = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) handleFile(file);
  }, [handleFile]);

  const handleRemove = useCallback(() => {
    setSelectedFile(null);
    setPreview(null);
    setError(null);
    onImageSelect(null);
  }, [onImageSelect]);

  return (
    <div className="w-full">
      {!preview ? (
        <div
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          className={`
            relative border-2 border-dashed rounded-lg p-12 text-center transition-all
            ${isDragging ? 'border-primary bg-secondary/50' : 'border-border bg-card'}
            ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer hover:border-primary hover:bg-secondary/30'}
          `}
        >
          <input
            type="file"
            accept="image/jpeg,image/png,image/heic"
            onChange={handleFileInput}
            disabled={disabled}
            className="absolute inset-0 w-full h-full opacity-0 cursor-pointer disabled:cursor-not-allowed"
          />
          <Upload className="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
          <p className="text-lg font-semibold text-foreground mb-2">
            메뉴판 사진을 업로드하세요
          </p>
          <p className="text-sm text-muted-foreground mb-4">
            드래그 앤 드롭 또는 클릭하여 선택
          </p>
          <p className="text-xs text-muted-foreground">
            JPG, PNG, HEIC • 최대 10MB
          </p>
        </div>
      ) : (
        <div className="relative rounded-lg overflow-hidden bg-card border border-border shadow-md">
          <img
            src={preview}
            alt="Menu preview"
            className="w-full h-auto max-h-[400px] object-contain"
          />
          <Button
            onClick={handleRemove}
            disabled={disabled}
            variant="destructive"
            size="icon"
            className="absolute top-4 right-4 rounded-full"
          >
            <X className="w-5 h-5" />
          </Button>
          <div className="p-4 bg-gradient-card">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <ImageIcon className="w-4 h-4" />
              <span>{selectedFile?.name}</span>
            </div>
          </div>
        </div>
      )}

      {error && (
        <p className="mt-2 text-sm text-destructive">{error}</p>
      )}
    </div>
  );
}
