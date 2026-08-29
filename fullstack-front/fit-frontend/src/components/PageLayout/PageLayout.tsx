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
  const shellClass = ["page-shell", className].filter(Boolean).join(" ");
  const contentClass = ["page-shell-content", `page-layout--${width}`].join(" ");

  return (
    <div className={shellClass}>
      {withNav && <NavBar />}
      <div className={contentClass}>{children}</div>
    </div>
  );
}

export default PageLayout;
