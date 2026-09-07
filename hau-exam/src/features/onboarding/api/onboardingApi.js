import { api } from "../../../services/api/client";

export const onboardingApi = {
  current: () => api.get("/api/v1/users/me/onboarding"),
  complete: (version) =>
    api.post("/api/v1/users/me/onboarding/complete", { version }),
};
