import { api } from "../../../services/api/client";

export const brandingApi = {
  public: () => api.get("/api/v1/public/system-branding"),
  admin: () => api.get("/api/v1/admin/platform/branding"),
  save: values => api.put("/api/v1/admin/platform/branding", values),
  uploadLogo: file => {
    const body = new FormData(); body.append("file", file, file.name);
    return api.post("/api/v1/admin/platform/branding/logo", body);
  },
  uploadFavicon: file => {
    const body = new FormData(); body.append("file", file, file.name);
    return api.post("/api/v1/admin/platform/branding/favicon", body);
  },
};
