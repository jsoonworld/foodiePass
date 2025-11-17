import { Component, ReactNode } from 'react';
import { AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

/**
 * Error Boundary component to catch and handle React errors gracefully
 * Prevents entire app from crashing when errors occur
 */
export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null });
    window.location.href = '/';
  };

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return (
        <div className="min-h-screen bg-background flex items-center justify-center p-4">
          <div className="max-w-md w-full">
            <Alert variant="destructive" className="mb-4">
              <AlertCircle className="h-4 w-4" />
              <AlertTitle>문제가 발생했습니다</AlertTitle>
              <AlertDescription>
                {this.state.error?.message || '알 수 없는 오류가 발생했습니다'}
              </AlertDescription>
            </Alert>

            <div className="flex flex-col gap-2">
              <Button onClick={this.handleReset} className="w-full">
                홈으로 돌아가기
              </Button>
              <Button
                onClick={() => window.location.reload()}
                variant="outline"
                className="w-full"
              >
                페이지 새로고침
              </Button>
            </div>

            {process.env.NODE_ENV === 'development' && this.state.error && (
              <details className="mt-4 p-4 bg-muted rounded-lg text-xs">
                <summary className="cursor-pointer font-semibold mb-2">
                  개발자 정보
                </summary>
                <pre className="whitespace-pre-wrap overflow-auto">
                  {this.state.error.stack}
                </pre>
              </details>
            )}
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
