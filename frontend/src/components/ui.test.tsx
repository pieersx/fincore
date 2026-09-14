import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import { ThemeToggle } from "./ui";

afterEach(() => {
  cleanup();
});

describe("ThemeToggle", () => {
  it("renders with role switch/aria-checked reflecting current theme", () => {
    const { rerender } = render(<ThemeToggle theme="light" onToggle={vi.fn()} />);

    expect(screen.getByRole("switch")).toHaveAttribute("aria-checked", "false");

    rerender(<ThemeToggle theme="dark" onToggle={vi.fn()} />);

    expect(screen.getByRole("switch")).toHaveAttribute("aria-checked", "true");
  });

  it("fires onToggle on click (native button provides Enter/Space activation for free)", () => {
    const onToggle = vi.fn();
    render(<ThemeToggle theme="light" onToggle={onToggle} />);

    const toggle = screen.getByRole("switch");
    // A native <button> is required so that browsers and assistive tech
    // translate Enter/Space keypresses into a click event automatically;
    // jsdom does not simulate that translation, so fireEvent.click stands in
    // here as the observable proxy for both mouse and keyboard activation.
    expect(toggle.tagName).toBe("BUTTON");

    fireEvent.click(toggle);
    expect(onToggle).toHaveBeenCalledTimes(1);

    fireEvent.click(toggle);
    expect(onToggle).toHaveBeenCalledTimes(2);
  });
});
