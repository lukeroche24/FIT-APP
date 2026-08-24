import type { ReactNode } from "react";
import NavBar from "../NavBar/NavBar";
import "./PageLayout.css";

export type PageWidth = "narrow" | "standard" | "wide" | "auth";

interface PageLayoutProps {
  width?: PageWidth;
  withNav?: boolean;
  className?: string;
  children: ReactNode;
}

function PageLayout({
  width = "standard",
  withNav = true,
  className = "",
  children,
}: PageLayoutProps) {
  const layoutClass = ["container", "page-layout", `page-layout--${width}`, className]
    .filter(Boolean)
    .join(" ");

  return (
    <div className={layoutClass}>
      {withNav && <NavBar />}
      {children}
    </div>
  );
}

export default PageLayout;
