import { useEffect, useState } from 'react';
import { Coins } from 'lucide-react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { getCurrencies } from '@/lib/api';
import type { CurrencyResponse } from '@/lib/types';

interface CurrencySelectorProps {
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
}

export default function CurrencySelector({ value, onChange, disabled }: CurrencySelectorProps) {
  const [currencies, setCurrencies] = useState<CurrencyResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchCurrencies() {
      try {
        const data = await getCurrencies();
        setCurrencies(data);
      } catch (error) {
        console.error('Failed to fetch currencies:', error);
      } finally {
        setLoading(false);
      }
    }
    fetchCurrencies();
  }, []);

  return (
    <div className="w-full">
      <label className="flex items-center gap-2 text-sm font-semibold text-foreground mb-2">
        <Coins className="w-4 h-4" />
        환율 변환
      </label>
      <Select value={value} onValueChange={onChange} disabled={disabled || loading}>
        <SelectTrigger className="w-full bg-card">
          <SelectValue placeholder={loading ? "로딩 중..." : "화폐를 선택하세요"} />
        </SelectTrigger>
        <SelectContent className="max-h-[300px] bg-popover z-50">
          {currencies.map((currency) => (
            <SelectItem key={currency.currencyName} value={currency.currencyName}>
              {currency.currencyName}
              {currency.currencyCode && ` (${currency.currencyCode})`}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}
