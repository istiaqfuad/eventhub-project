import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: "/api/backend/:path*",
        destination: "http://localhost:8080/api/:path*", // Removed v1 based on SecurityPaths
      },
    ];
  },
};

export default nextConfig;
