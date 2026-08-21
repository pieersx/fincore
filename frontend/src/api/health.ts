export interface HealthResponse {
  status: string;
}

function isHealthResponse(value: unknown): value is HealthResponse {
  return (
    typeof value === "object" &&
    value !== null &&
    "status" in value &&
    typeof value.status === "string"
  );
}

/** Reads the backend health contract without trusting an unvalidated JSON body. */
export async function getBackendHealth(signal?: AbortSignal): Promise<HealthResponse> {
  const response = await fetch("/actuator/health", {
    headers: { Accept: "application/json" },
    signal,
  });

  if (!response.ok) {
    throw new Error(`Backend health request failed with HTTP ${response.status}`);
  }

  const body: unknown = await response.json();
  if (!isHealthResponse(body)) {
    throw new Error("Backend health response has an invalid shape");
  }

  return body;
}
