import { useState } from 'react';
import { Camera } from 'lucide-react';
import { Button } from '@/components/ui/button';
import LanguageSelector from '@/components/LanguageSelector';
import CurrencySelector from '@/components/CurrencySelector';
import MenuUploader from '@/components/MenuUploader';

const Index = () => {
  const [selectedLanguage, setSelectedLanguage] = useState<string>('');
  const [selectedCurrency, setSelectedCurrency] = useState<string>('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const canSubmit = selectedLanguage && selectedCurrency && selectedFile && !isSubmitting;

  const handleSubmit = async () => {
    if (!canSubmit) return;

    setIsSubmitting(true);
    try {
      console.log('Submitting:', {
        language: selectedLanguage,
        currency: selectedCurrency,
        file: selectedFile?.name,
      });
      // TODO: Implement menu scan API call
    } catch (error) {
      console.error('Failed to submit:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-secondary/5">
      <div className="container max-w-2xl mx-auto px-4 py-12">
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-3 mb-4">
            <Camera className="w-10 h-10 text-primary" />
            <h1 className="text-4xl font-bold text-foreground">FoodiePass</h1>
          </div>
          <p className="text-lg text-muted-foreground">
            메뉴판 사진을 업로드하면 번역과 음식 정보를 제공합니다
          </p>
        </div>

        <div className="bg-card rounded-lg shadow-lg border border-border p-6 space-y-6">
          <LanguageSelector
            value={selectedLanguage}
            onChange={setSelectedLanguage}
            disabled={isSubmitting}
          />

          <CurrencySelector
            value={selectedCurrency}
            onChange={setSelectedCurrency}
            disabled={isSubmitting}
          />

          <MenuUploader
            onImageSelect={setSelectedFile}
            disabled={isSubmitting}
          />

          <Button
            onClick={handleSubmit}
            disabled={!canSubmit}
            className="w-full h-12 text-lg font-semibold"
            size="lg"
          >
            {isSubmitting ? '처리 중...' : '메뉴 스캔하기'}
          </Button>
        </div>

        <div className="mt-6 text-center text-sm text-muted-foreground">
          <p>JPG, PNG, HEIC 형식 지원 • 최대 10MB</p>
        </div>
      </div>
    </div>
  );
};

export default Index;
