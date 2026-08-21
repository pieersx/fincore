import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import App from "./App";

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("App", () => {
  it("reports a healthy backend connection", async () => {
    const fetchMock = vi.fn<typeof fetch>().mockResolvedValue(
      new Response(JSON.stringify({ status: "UP" }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }),
    );
    vi.stubGlobal("fetch", fetchMock);

    render(<App />);

    expect(await screen.findByText("Backend conectado")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith(
      "/actuator/health",
      expect.objectContaining({ signal: expect.any(AbortSignal) }),
    );
  });

  it("allows the connection to be retried after a failure", async () => {
    const fetchMock = vi
      .fn<typeof fetch>()
      .mockRejectedValueOnce(new TypeError("Connection refused"))
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ status: "UP" }), {
          status: 200,
          headers: { "Content-Type": "application/json" },
        }),
      );
    vi.stubGlobal("fetch", fetchMock);

    render(<App />);

    expect(await screen.findByText("Backend no disponible")).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Reintentar conexión" }));

    expect(await screen.findByText("Backend conectado")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });
});
