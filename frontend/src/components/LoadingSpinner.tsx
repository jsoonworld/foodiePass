import { Loader2 } from 'lucide-react';

interface LoadingSpinnerProps {
  message?: string;
  subMessage?: string;
}

export default function LoadingSpinner({ 
  message = "메뉴를 분석하는 중...", 
  subMessage = "보통 5초 정도 걸립니다." 
}: LoadingSpinnerProps) {
  return (
    <div
      className="fixed inset-0 bg-background/80 backdrop-blur-sm flex items-center justify-center z-50"
      role="status"
      aria-live="polite"
      aria-busy="true"
      aria-label={message}
    >
      <div className="text-center">
        <Loader2 className="w-12 h-12 animate-spin text-primary mx-auto mb-4" aria-hidden="true" />
        <p className="text-lg font-semibold text-foreground mb-2">{message}</p>
        <p className="text-sm text-muted-foreground">{subMessage}</p>
      </div>
    </div>
  );
}
