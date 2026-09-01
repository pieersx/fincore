import { afterEach, describe, expect, it, vi } from "vitest";

import { apiRequest, clearCsrf } from "./client";

afterEach(() => {
  clearCsrf();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});

describe("cliente HTTP", () => {
  it("obtiene CSRF y lo incluye en una escritura JSON", async () => {
    const fetchMock = vi
      .fn<typeof fetch>()
      .mockResolvedValueOnce(new Response(JSON.stringify({ token: "token-123", headerName: "X-CSRF-TOKEN", parameterName: "_csrf" }), { status: 200, headers: { "Content-Type": "application/json" } }))
      .mockResolvedValueOnce(new Response(JSON.stringify({ id: "beneficiary-1" }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await apiRequest<{ id: string }>("/beneficiaries", { method: "POST", body: { alias: "Ana" } });

    expect(fetchMock).toHaveBeenCalledTimes(2);
    const [, request] = fetchMock.mock.calls[1];
    const headers = new Headers(request?.headers);
    expect(headers.get("X-CSRF-TOKEN")).toBe("token-123");
    expect(headers.get("Content-Type")).toBe("application/json");
    expect(request?.credentials).toBe("include");
    expect(request?.body).toBe(JSON.stringify({ alias: "Ana" }));
  });
});
