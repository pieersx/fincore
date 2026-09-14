import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import { ThemeProvider } from "./ThemeProvider";
import { useTheme } from "./ThemeContext";

const STORAGE_KEY = "fincore-theme";

function stubMatchMedia(prefersDark: boolean) {
  vi.stubGlobal(
    "matchMedia",
    vi.fn().mockImplementation((query: string) => ({
      matches: query === "(prefers-color-scheme: dark)" ? prefersDark : false,
      media: query,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    })),
  );
}

function ThemeProbe() {
  const { theme, toggleTheme } = useTheme();
  return (
    <>
      <span data-testid="theme-value">{theme}</span>
      <button type="button" onClick={toggleTheme}>
        toggle
      </button>
    </>
  );
}

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
  localStorage.clear();
});

describe("resolución del tema inicial", () => {
  it("resolves dark when OS prefers dark and no stored preference", () => {
    stubMatchMedia(true);

    render(
      <ThemeProvider>
        <ThemeProbe />
      </ThemeProvider>,
    );

    expect(screen.getByTestId("theme-value")).toHaveTextContent("dark");
  });

  it("resolves light when OS prefers light and no stored preference", () => {
    stubMatchMedia(false);

    render(
      <ThemeProvider>
        <ThemeProbe />
      </ThemeProvider>,
    );

    expect(screen.getByTestId("theme-value")).toHaveTextContent("light");
  });
});

describe("cambio manual de tema y persistencia", () => {
  it("toggle switches theme value", () => {
    stubMatchMedia(false);

    render(
      <ThemeProvider>
        <ThemeProbe />
      </ThemeProvider>,
    );

    expect(screen.getByTestId("theme-value")).toHaveTextContent("light");

    fireEvent.click(screen.getByRole("button", { name: "toggle" }));

    expect(screen.getByTestId("theme-value")).toHaveTextContent("dark");
  });

  it("reads persisted preference on mount, ignoring current OS preference", () => {
    localStorage.setItem(STORAGE_KEY, "dark");
    stubMatchMedia(false);

    render(
      <ThemeProvider>
        <ThemeProbe />
      </ThemeProvider>,
    );

    expect(screen.getByTestId("theme-value")).toHaveTextContent("dark");
  });

  it("persists a manually toggled theme so a second provider instance picks it up", () => {
    stubMatchMedia(false);

    const first = render(
      <ThemeProvider>
        <ThemeProbe />
      </ThemeProvider>,
    );

    fireEvent.click(first.getByRole("button", { name: "toggle" }));
    expect(first.getByTestId("theme-value")).toHaveTextContent("dark");
    first.unmount();

    const second = render(
      <ThemeProvider>
        <ThemeProbe />
      </ThemeProvider>,
    );

    expect(second.getByTestId("theme-value")).toHaveTextContent("dark");
  });
});
