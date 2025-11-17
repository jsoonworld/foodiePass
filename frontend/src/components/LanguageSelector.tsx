import { useEffect, useState } from 'react';
import { Languages } from 'lucide-react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { getLanguages } from '@/lib/api';
import type { LanguageResponse } from '@/lib/types';

interface LanguageSelectorProps {
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
}

export default function LanguageSelector({ value, onChange, disabled }: LanguageSelectorProps) {
  const [languages, setLanguages] = useState<LanguageResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchLanguages() {
      try {
        const data = await getLanguages();
        setLanguages(data);
      } catch (error) {
        console.error('Failed to fetch languages:', error);
      } finally {
        setLoading(false);
      }
    }
    fetchLanguages();
  }, []);

  return (
    <div className="w-full">
      <label className="flex items-center gap-2 text-sm font-semibold text-foreground mb-2">
        <Languages className="w-4 h-4" />
        번역할 언어
      </label>
      <Select value={value} onValueChange={onChange} disabled={disabled || loading}>
        <SelectTrigger className="w-full bg-card">
          <SelectValue placeholder={loading ? "로딩 중..." : "언어를 선택하세요"} />
        </SelectTrigger>
        <SelectContent className="max-h-[300px] bg-popover z-50">
          {languages.map((lang) => (
            <SelectItem key={lang.languageName} value={lang.languageName}>
              {lang.languageName}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}
