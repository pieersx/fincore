import type { Currency } from "../types/api";

const currencyFormatters: Record<Currency, Intl.NumberFormat> = {
  PEN: new Intl.NumberFormat("es-PE", { style: "currency", currency: "PEN" }),
  USD: new Intl.NumberFormat("es-PE", { style: "currency", currency: "USD" }),
};

export function formatMoney(value: number, currency: Currency): string {
  return currencyFormatters[currency].format(value);
}

export function formatDate(value: string): string {
  return new Intl.DateTimeFormat("es-PE", {
    dateStyle: "medium",
    timeStyle: "short",
    timeZone: "America/Lima",
  }).format(new Date(value));
}

export function maskAccount(value: string): string {
  return `${value.slice(0, 5)} •••• ${value.slice(-4)}`;
}

export function shortId(value: string): string {
  return value.slice(0, 8);
}
