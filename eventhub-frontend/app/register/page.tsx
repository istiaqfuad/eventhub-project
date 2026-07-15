"use client";

import { useState } from "react";
import Link from "next/link";
import { Ticket } from "lucide-react";
import { getProblemDetail, useRegister } from "../hooks/useAuth";

export default function RegisterPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const { mutate: register, isPending, error } = useRegister();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    register({ email, password });
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-[#0b0e14] text-white p-4">
      <div className="w-full max-w-md bg-[#151a23] rounded-2xl p-8 border border-white/10">
        <div className="text-center mb-8">
          <Link
            href="/"
            className="inline-flex items-center gap-2 mb-4 text-[#00f0ff]"
          >
            <Ticket size={24} />
            <span className="font-extrabold text-xl">EventHub</span>
          </Link>
          <h1 className="text-3xl font-bold mb-2">Create Account</h1>
          <p className="text-gray-400">Join the best platform for live entertainment.</p>
        </div>

        {error && <div className="bg-[#ff3366]/10 border border-[#ff3366]/30 text-[#ff8fab] p-4 rounded-lg mb-6">{getProblemDetail(error)}</div>}

        <form onSubmit={handleSubmit} className="flex flex-col gap-6">
          <div className="flex flex-col gap-2">
            <label htmlFor="email" className="font-semibold text-sm">Email Address</label>
            <input
              type="email"
              id="email"
              className="w-full bg-[#202632] border border-white/10 rounded-lg p-3 text-white focus:border-[#00f0ff] focus:outline-none transition-colors"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
              placeholder="you@example.com"
            />
          </div>

          <div className="flex flex-col gap-2">
            <label htmlFor="password" className="font-semibold text-sm">Password</label>
            <input
              type="password"
              id="password"
              className="w-full bg-[#202632] border border-white/10 rounded-lg p-3 text-white focus:border-[#00f0ff] focus:outline-none transition-colors"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="new-password"
              placeholder="At least 8 characters"
              minLength={8}
            />
            <p className="text-xs text-gray-400 mt-1">
              Must be 8–72 characters and not appear in known data breaches.
            </p>
          </div>

          <button
            type="submit"
            className="w-full bg-gradient-to-r from-[#00f0ff] to-[#7000ff] text-white font-semibold py-3 px-4 rounded-lg hover:shadow-[0_4px_15px_rgba(112,0,255,0.4)] hover:-translate-y-0.5 transition-all disabled:opacity-50"
            disabled={isPending}
          >
            {isPending ? "Creating Account..." : "Create Account"}
          </button>
        </form>

        <div className="text-center mt-8 text-gray-400 text-sm">
          Already have an account? <Link href="/login" className="text-[#00f0ff] hover:underline">Sign in</Link>
        </div>
      </div>
    </div>
  );
}
