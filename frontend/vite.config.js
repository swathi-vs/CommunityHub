import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Dev only: forwards API calls to Tomcat so the browser sees one origin (the backend sends no CORS headers).
const tomcat = "http://localhost:8080";
const paths = ["login", "signup", "auth", "roles", "property-types", "organization", "organizations"];

export default defineConfig({
  base: "./",
  plugins: [react()],
  server: { proxy: Object.fromEntries(paths.map((p) => [`/CommunityHub/${p}`, tomcat])) },
});
