import { useBranding } from "./brandingContext";

export function BrandLogo({ className = "", alt, ...props }) {
  const branding = useBranding();
  return <img className={className} src={branding.logo} alt={alt ?? `${branding.shortName} logo`} {...props} />;
}
