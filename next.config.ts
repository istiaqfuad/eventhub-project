import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: "/api/backend/:path*",
        destination: "http://localhost:8080/api/v1/:path*", // Assuming v1 is the base path
      },
    ];
  },
};

export default nextConfig;
