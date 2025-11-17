import { useLocation, useNavigate } from 'react-router-dom';
import { ArrowLeft, Clock } from 'lucide-react';
import { Button } from '@/components/ui/button';
import ControlMenu from '@/components/ControlMenu';
import TreatmentMenu from '@/components/TreatmentMenu';
import SurveyModal from '@/components/SurveyModal';
import type { MenuScanResponse } from '@/lib/types';

export default function MenuResultPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const result = location.state?.result as MenuScanResponse | undefined;

  if (!result) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center p-4">
        <div className="text-center">
          <p className="text-lg text-foreground mb-4">결과를 찾을 수 없습니다</p>
          <Button onClick={() => navigate('/')}>홈으로 돌아가기</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="container max-w-6xl mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-8">
          <Button
            onClick={() => navigate('/')}
            variant="outline"
            className="gap-2"
          >
            <ArrowLeft className="w-4 h-4" />
            다시 스캔하기
          </Button>
          
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Clock className="w-4 h-4" />
            <span>{result.processingTime.toFixed(1)}초</span>
          </div>
        </div>

        <div className="mb-8">
          <h1 className="text-3xl font-bold text-foreground mb-2">
            메뉴 분석 완료
          </h1>
          <p className="text-muted-foreground">
            {result.items.length}개의 메뉴를 찾았습니다
          </p>
        </div>

        {result.abGroup === 'CONTROL' ? (
          <ControlMenu items={result.items} />
        ) : (
          <TreatmentMenu items={result.items} />
        )}

        <SurveyModal scanId={result.scanId} delay={5000} />
      </div>
    </div>
  );
}
