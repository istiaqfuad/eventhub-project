import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      // Matches refresh cookie Path=/api/auth set by the backend
      {
        source: "/api/auth/:path*",
        destination: "http://localhost:8080/api/auth/:path*",
      },
      {
        source: "/api/backend/:path*",
        destination: "http://localhost:8080/api/:path*",
      },
    ];
  },
};

export default nextConfig;
