# FoodiePass Frontend - Component Design

> **목적**: React/Next.js 컴포넌트 구조 및 설계

---

## 디렉토리 구조 (권장)

```
src/
├── app/                      # Next.js App Router
│   ├── page.tsx              # 홈 (업로더)
│   ├── menu/[scanId]/page.tsx # 메뉴 표시
│   └── layout.tsx
├── components/
│   ├── MenuUploader/         # F-01: 업로더
│   │   ├── index.tsx
│   │   ├── ImageInput.tsx
│   │   └── LanguageCurrencySelector.tsx
│   ├── MenuDisplay/
│   │   ├── TextOnlyMenu.tsx  # F-07: Control UI
│   │   ├── VisualMenu.tsx    # F-06: Treatment UI
│   │   └── MenuCard.tsx
│   ├── Survey/
│   │   └── SurveyModal.tsx   # F-09: 설문
│   └── common/
│       ├── LoadingSpinner.tsx
│       └── ErrorMessage.tsx
├── lib/
│   ├── api.ts                # API 클라이언트
│   └── types.ts              # TypeScript 타입
└── hooks/
    ├── useMenuScan.ts
    └── useSurvey.ts
```

---

## 주요 컴포넌트

### 1. MenuUploader (F-01)

**역할**: 메뉴판 이미지 업로드 + 언어/화폐 선택

```tsx
// components/MenuUploader/index.tsx
export function MenuUploader() {
  const [base64EncodedImage, setBase64EncodedImage] = useState<string | null>(null);
  const [userLanguageName, setUserLanguageName] = useState('Korean');
  const [userCurrencyName, setUserCurrencyName] = useState('KRW Won');
  const { scan, loading } = useMenuScan();

  const handleSubmit = async () => {
    const result = await scan({
      base64EncodedImage,
      userLanguageName,
      userCurrencyName
    });
    router.push(`/menu/${result.scanId}`);
  };

  return (
    <div>
      <ImageInput onChange={setBase64EncodedImage} />
      <LanguageCurrencySelector
        onLanguageChange={setUserLanguageName}
        onCurrencyChange={setUserCurrencyName}
      />
      <button onClick={handleSubmit} disabled={!base64EncodedImage || loading}>
        {loading ? 'Processing...' : 'Scan Menu'}
      </button>
    </div>
  );
}
```

---

### 2. TextOnlyMenu (F-07, Control)

**역할**: 텍스트 + 환율만 표시

```tsx
// components/MenuDisplay/TextOnlyMenu.tsx
export function TextOnlyMenu({ items }: { items: MenuItem[] }) {
  return (
    <ul>
      {items.map((item) => (
        <li key={item.id}>
          <div>
            <strong>{item.originalName}</strong>
            <span>{item.translatedName}</span>
          </div>
          <div>
            <span>{item.priceInfo.originalFormatted}</span>
            <span>({item.priceInfo.convertedFormatted})</span>
          </div>
        </li>
      ))}
    </ul>
  );
}
```

---

### 3. VisualMenu (F-06, Treatment)

**역할**: 사진 + 설명 + 텍스트 + 환율 표시

```tsx
// components/MenuDisplay/VisualMenu.tsx
export function VisualMenu({ items }: { items: MenuItem[] }) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {items.map((item) => (
        <div key={item.id} className="card">
          {item.imageUrl && (
            <img src={item.imageUrl} alt={item.translatedName} />
          )}
          <h3>{item.originalName}</h3>
          <p>{item.translatedName}</p>
          {item.description && <p className="text-sm">{item.description}</p>}
          <div>
            <span>{item.priceInfo.originalFormatted}</span>
            <span>({item.priceInfo.convertedFormatted})</span>
          </div>
        </div>
      ))}
    </div>
  );
}
```

---

### 4. SurveyModal (F-09)

**역할**: 확신도 설문 표시 및 제출

```tsx
// components/Survey/SurveyModal.tsx
export function SurveyModal({ scanId, onClose }: Props) {
  const { submit, loading } = useSurvey();

  const handleAnswer = async (hasConfidence: boolean) => {
    await submit(scanId, hasConfidence);
    onClose();
  };

  return (
    <div className="modal">
      <h2>이 정보만으로 확신을 갖고 주문할 수 있습니까?</h2>
      <div>
        <button onClick={() => handleAnswer(true)} disabled={loading}>
          Yes (확신 있음)
        </button>
        <button onClick={() => handleAnswer(false)} disabled={loading}>
          No (여전히 불안함)
        </button>
      </div>
    </div>
  );
}
```

---

### 5. MenuPage (통합)

**역할**: abGroup에 따라 다른 UI 렌더링

```tsx
// app/menu/[scanId]/page.tsx
export default function MenuPage({ params }: { params: { scanId: string } }) {
  const { result } = useMenuScan();
  const [showSurvey, setShowSurvey] = useState(false);

  useEffect(() => {
    // 메뉴 표시 후 5초 뒤 설문 표시
    const timer = setTimeout(() => setShowSurvey(true), 5000);
    return () => clearTimeout(timer);
  }, []);

  if (!result) return <LoadingSpinner />;

  return (
    <div>
      <h1>Menu</h1>
      {result.abGroup === 'CONTROL' ? (
        <TextOnlyMenu items={result.items} />
      ) : (
        <VisualMenu items={result.items} />
      )}

      {showSurvey && (
        <SurveyModal
          scanId={result.scanId}
          onClose={() => setShowSurvey(false)}
        />
      )}
    </div>
  );
}
```

---

## 상태 관리

**권장**: React Context 또는 Zustand (간단한 상태만 필요)

```tsx
// context/MenuScanContext.tsx
const MenuScanContext = createContext<{
  scanResult: MenuScanResponse | null;
  setScanResult: (result: MenuScanResponse) => void;
}>({
  scanResult: null,
  setScanResult: () => {},
});
```

---

## 스타일링

**권장**: Tailwind CSS (빠른 프로토타입)

```tsx
<div className="container mx-auto p-4">
  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
    {/* 메뉴 카드들 */}
  </div>
</div>
```

---

## 참고 문서

- [USER_FLOW.md](./USER_FLOW.md) - 사용자 플로우
- [UI_REQUIREMENTS.md](./UI_REQUIREMENTS.md) - UI/UX 요구사항
- [API_INTEGRATION.md](./API_INTEGRATION.md) - API 연동
