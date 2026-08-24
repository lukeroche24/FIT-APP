interface ErrorBannerProps {
  message: string | null;
}

function ErrorBanner({ message }: ErrorBannerProps) {
  if (!message) {
    return null;
  }

  return <div className="alert alert-danger">{message}</div>;
}

export default ErrorBanner;
