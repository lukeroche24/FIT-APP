import "./BrandLogo.css";

type BrandLogoProps = {
  variant?: "nav" | "auth";
};

function FitMark() {
  return (
    <svg className="brand-logo-mark" viewBox="0 0 24 18" aria-hidden="true">
      <rect x="0" y="0" width="10" height="5" rx="0.9" fill="#FF4F6D" />
      <rect x="0" y="6.5" width="17" height="5" rx="0.9" fill="#FF4F6D" />
      <rect x="0" y="13" width="24" height="5" rx="0.9" fill="#FF4F6D" />
    </svg>
  );
}

export default function BrandLogo({ variant = "nav" }: BrandLogoProps) {
  return (
    <span className={`brand-logo brand-logo-${variant}`}>
      <FitMark />
      <span className="brand-logo-word">FIT</span>
    </span>
  );
}
