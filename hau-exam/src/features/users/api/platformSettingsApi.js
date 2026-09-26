import { api } from "../../../services/api/client";

export const platformSettingsApi = {
  cloudinaryStatus: () => api.get("/api/v1/admin/platform/cloudinary"),
  webTraffic: () => api.get("/api/v1/admin/analytics/traffic"),
};
