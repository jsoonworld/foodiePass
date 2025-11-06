import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Utensils, Scan } from 'lucide-react';
import { Button } from '@/components/ui/button';
import MenuUploader from '@/components/MenuUploader';
import LanguageSelector from '@/components/LanguageSelector';
import CurrencySelector from '@/components/CurrencySelector';
import LoadingSpinner from '@/components/LoadingSpinner';
import ErrorMessage from '@/components/ErrorMessage';
import { useMenuScan } from '@/hooks/useMenuScan';
import { fileToBase64 } from '@/utils/imageUtils';

export default function HomePage() {
  const navigate = useNavigate();
  const { scan, loading, error } = useMenuScan();
  
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [language, setLanguage] = useState('Korean');
  const [currency, setCurrency] = useState('South Korean won');

  const handleScan = async () => {
    if (!selectedFile) return;

    try {
      const base64Image = await fileToBase64(selectedFile);
      const result = await scan({
        base64EncodedImage: base64Image,
        userLanguageName: language,
        userCurrencyName: currency,
      });
      
      navigate(`/menu/${result.scanId}`, { state: { result } });
    } catch (err) {
      console.error('Scan failed:', err);
    }
  };

  const canSubmit = selectedFile && language && currency && !loading;

  return (
    <div className="min-h-screen bg-background">
      {loading && <LoadingSpinner />}
      
      <div className="container max-w-4xl mx-auto px-4 py-8">
        <header className="text-center mb-12">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-hero rounded-full mb-4 shadow-lg">
            <Utensils className="w-8 h-8 text-primary-foreground" />
          </div>
          <h1 className="text-4xl md:text-5xl font-bold text-foreground mb-3">
            FoodiePass
          </h1>
          <p className="text-lg text-muted-foreground">
            메뉴판을 찍으면 자동으로 번역하고 사진까지 보여드립니다
          </p>
        </header>

        <div className="bg-card border border-border rounded-2xl p-6 md:p-8 shadow-lg space-y-6">
          <MenuUploader
            onImageSelect={setSelectedFile}
            disabled={loading}
          />

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <LanguageSelector
              value={language}
              onChange={setLanguage}
              disabled={loading}
            />
            <CurrencySelector
              value={currency}
              onChange={setCurrency}
              disabled={loading}
            />
          </div>

          {error && (
            <ErrorMessage message={error} onRetry={handleScan} />
          )}

          <Button
            onClick={handleScan}
            disabled={!canSubmit}
            className="w-full h-14 text-lg font-semibold bg-gradient-hero hover:opacity-90 transition-opacity"
          >
            <Scan className="w-5 h-5 mr-2" />
            메뉴 스캔하기
          </Button>
        </div>

        <footer className="mt-12 text-center text-sm text-muted-foreground">
          <p>JPG, PNG, HEIC 형식 지원 • 최대 10MB</p>
          <p className="mt-2">평균 처리 시간: 5초</p>
        </footer>
      </div>
    </div>
  );
}
