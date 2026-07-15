import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { QueryProvider } from "./providers/query-provider";
import { AuthStoreProvider } from "./providers/auth-store-provider";
import SessionRestore from "./components/SessionRestore";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-main",
});

export const metadata: Metadata = {
  title: "EventHub | Discover & Book Live Events",
  description: "A premium ticketing platform for concerts, sports, and live entertainment.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={inter.variable}>
        <QueryProvider>
          <AuthStoreProvider>
            <SessionRestore />
            {children}
          </AuthStoreProvider>
        </QueryProvider>
      </body>
    </html>
  );
}
