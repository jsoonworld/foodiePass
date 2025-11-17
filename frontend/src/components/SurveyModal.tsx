import { useEffect, useState, useRef } from 'react';
import { CheckCircle2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useSurvey } from '@/hooks/useSurvey';

interface SurveyModalProps {
  scanId: string;
  delay?: number;
}

export default function SurveyModal({ scanId, delay = 5000 }: SurveyModalProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const { submit, loading, error } = useSurvey();
  const closeTimerRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsOpen(true);
    }, delay);

    return () => {
      clearTimeout(timer);
      if (closeTimerRef.current) {
        clearTimeout(closeTimerRef.current);
      }
    };
  }, [delay]);

  const handleSubmit = async (hasConfidence: boolean) => {
    try {
      await submit(scanId, hasConfidence);
      setSubmitted(true);
      closeTimerRef.current = setTimeout(() => {
        setIsOpen(false);
      }, 2000);
    } catch (error) {
      console.error('Failed to submit survey:', error);
      // Error is already handled by useSurvey hook and displayed below
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-background/80 backdrop-blur-sm flex items-center justify-center z-50 p-4" data-testid="survey-modal">
      <div className="bg-card border border-border rounded-xl shadow-lg max-w-md w-full p-6 animate-in fade-in slide-in-from-bottom-4">
        {!submitted ? (
          <>
            <h2 className="text-xl font-bold text-foreground mb-4 text-center">
              확신도 설문
            </h2>
            <p className="text-foreground mb-6 text-center">
              이 정보만으로 확신을 갖고 주문할 수 있습니까?
            </p>
            {error && (
              <p className="text-destructive text-sm text-center mb-4">{error}</p>
            )}
            <div className="flex gap-3">
              <Button
                onClick={() => handleSubmit(true)}
                disabled={loading}
                className="flex-1 h-12"
                variant="default"
              >
                Yes
              </Button>
              <Button
                onClick={() => handleSubmit(false)}
                disabled={loading}
                className="flex-1 h-12"
                variant="outline"
              >
                No
              </Button>
            </div>
          </>
        ) : (
          <div className="text-center py-4">
            <CheckCircle2 className="w-16 h-16 text-primary mx-auto mb-4" />
            <p className="text-lg font-semibold text-foreground">
              감사합니다!
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
