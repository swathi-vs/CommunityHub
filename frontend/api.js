import { mockApi } from "./mock";

// Context path of the Tomcat app. For production, build into src/main/webapp/app/ so the origin is shared.
export const BASE = "/CommunityHub";

// Demo mode: answer every call from in-browser dummy data (mock.js) instead of the server.
//   npm run dev        -> dummy data (default)
//   npm run dev:real   -> real backend on Tomcat (needs Tomcat + MySQL running)
export const MOCK = import.meta.env.VITE_MOCK !== "false";

let onAuthLost = () => {};
export const setAuthLostHandler = (fn) => (onAuthLost = fn);

// Backend reads form fields, not JSON. Returns the { success, message, code, data?, errors? } envelope.
export async function api(path, params = {}, method = "POST") {
  const clean = Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== "").map(([k, v]) => [k, String(v)])
  );
  let json;
  if (MOCK) {
    json = await mockApi(path, clean, method);
  } else {
    const body = new URLSearchParams(clean);
    const isGet = method === "GET";
    try {
      const res = await fetch(isGet ? `${BASE}/${path}?${body}` : `${BASE}/${path}`, {
        method,
        credentials: "include",
        body: isGet ? undefined : body,
      });
      json = await res.json();
    } catch {
      return { success: false, code: 500, message: "Can’t reach the server. Check your connection and try again." };
    }
  }
  if (json.code === 401 && path !== "login") onAuthLost();
  return json;
}
export const get = (path, params) => api(path, params, "GET");
