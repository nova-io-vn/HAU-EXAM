import { createContext, useContext } from "react";
import defaultLogo from "../../assets/logo.jpg";

export const brandingDefaults = { systemName: "HAU QM", shortName: "HAU QM", logoUrl: null, faviconUrl: null };
export const BrandingContext = createContext({ ...brandingDefaults, logo: defaultLogo, refresh: async () => brandingDefaults, apply: () => undefined });
export function useBranding() { return useContext(BrandingContext); }
