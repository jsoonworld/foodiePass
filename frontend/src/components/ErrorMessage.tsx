import { AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';

interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
}

export default function ErrorMessage({ message, onRetry }: ErrorMessageProps) {
  return (
    <div className="bg-destructive/10 border border-destructive/20 rounded-lg p-6 text-center">
      <AlertCircle className="w-12 h-12 text-destructive mx-auto mb-4" />
      <p className="text-foreground font-semibold mb-2">오류가 발생했습니다</p>
      <p className="text-muted-foreground mb-4">{message}</p>
      {onRetry && (
        <Button onClick={onRetry} variant="destructive">
          다시 시도
        </Button>
      )}
    </div>
  );
}
