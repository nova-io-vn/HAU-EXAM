import { api } from "../../../services/api/client";

export const platformSettingsApi = {
  cloudinaryStatus: () => api.get("/api/v1/admin/platform/cloudinary"),
  saveCloudinary: settings => api.put("/api/v1/admin/platform/cloudinary", settings),
  vercelAnalyticsStatus: () => api.get("/api/v1/admin/platform/vercel-analytics"),
  saveVercelAnalytics: settings => api.put("/api/v1/admin/platform/vercel-analytics", settings),
  webTraffic: () => api.get("/api/v1/admin/analytics/traffic"),
};
