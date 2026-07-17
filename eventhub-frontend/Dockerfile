FROM node:20-alpine AS builder
WORKDIR /app
# Install pnpm
RUN npm install -g pnpm
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY . .
# Set the backend URL for the build
ARG NEXT_PUBLIC_API_URL=http://backend:8080/api/v1
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ARG BACKEND_URL=http://backend:8080
ENV BACKEND_URL=$BACKEND_URL
RUN pnpm build

FROM node:20-alpine AS runner
WORKDIR /app
# Install pnpm
RUN npm install -g pnpm
ENV NODE_ENV production
# Copy necessary files
COPY --from=builder /app/next.config.ts ./
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

EXPOSE 3000
CMD ["node", "server.js"]
