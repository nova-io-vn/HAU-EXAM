import { useCallback, useEffect, useMemo, useState } from "react";
import defaultLogo from "../../assets/logo.jpg";
import { brandingApi } from "./api/brandingApi";
import { BrandingContext, brandingDefaults as defaults } from "./brandingContext";

export function BrandingProvider({ children }) {
  const [branding, setBranding] = useState(defaults);
  const refresh = useCallback(async function refresh() {
    try {
      const value = await brandingApi.public();
      if (value) setBranding({ ...defaults, ...value });
      return value;
    } catch { return defaults; }
  }, []);
  useEffect(() => { const timer = setTimeout(() => void refresh(), 0); return () => clearTimeout(timer); }, [refresh]);
  useEffect(() => {
    document.title = branding.systemName;
    if (!branding.faviconUrl) return;
    let link = document.querySelector('link[rel="icon"]');
    if (!link) { link = document.createElement("link"); link.rel = "icon"; document.head.appendChild(link); }
    link.href = branding.faviconUrl;
  }, [branding]);
  const value = useMemo(() => ({ ...branding, logo: branding.logoUrl || defaultLogo, refresh, apply: value => setBranding({ ...defaults, ...value }) }), [branding, refresh]);
  return <BrandingContext.Provider value={value}>{children}</BrandingContext.Provider>;
}
