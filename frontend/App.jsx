import { useEffect, useState } from "react";
import { api, get, setAuthLostHandler } from "./api";
import { Form, FormModal, Page, day, money, poster } from "./ui";
import { Forms, Invoices, Payments, Posts, Properties, Settings, Tenancies, Users } from "./screens";
import "./App.css";

const OPEN = ["pending", "partial", "overdue"];

const store = {
  read: () => { try { return JSON.parse(sessionStorage.getItem("ch")); } catch { return null; } },
  write: (v) => { try { v ? sessionStorage.setItem("ch", JSON.stringify(v)) : sessionStorage.removeItem("ch"); } catch { /* storage unavailable */ } },
};

/* ---------- Sign in / sign up ---------- */
function Auth({ onLogin }) {
  const [mode, setMode] = useState("login");
  const login = (v) => api("login", { email: v.email, password: v.password });
  const loginFields = [
    { name: "email", label: "Email", type: "email", required: true },
    { name: "password", label: "Password", type: "password", required: true },
  ];
  const signupFields = [
    { name: "user.fullName", label: "Full name", required: true },
    { name: "user.email", label: "Email", type: "email", required: true },
    { name: "user.phone", label: "Phone (10 digits)", type: "tel", required: true },
    { name: "password", label: "Password (min 6 characters)", type: "password", required: true },
  ];
  const signup = async (v) => {
    const r = await api("signup", v);
    return r.success ? login({ email: v["user.email"], password: v.password }) : r;
  };
  return (
    <main className="auth">
      <div className="auth-head">
        <h1 className="brand">CommunityHub</h1>
        <p className="muted">Rent, rooms and notices for your hostel or PG.</p>
      </div>
      <Form key={mode} fields={mode === "login" ? loginFields : signupFields} submit={mode === "login" ? login : signup}
        label={mode === "login" ? "Sign in" : "Create account"} onSuccess={onLogin} />
      <button className="link" onClick={() => setMode(mode === "login" ? "signup" : "login")}>
        {mode === "login" ? "New here? Create an account" : "Have an account? Sign in"}
      </button>
    </main>
  );
}

/* ---------- Organization picker ---------- */
function OrgPicker({ session, onPick, onCreated, onLogout }) {
  const [creating, setCreating] = useState(false);
  const fields = [
    { name: "organization.name", label: "Organization name", required: true },
    { name: "organization.businessEmail", label: "Business email", type: "email", required: true },
    { name: "organization.businessPhone", label: "Business phone (10 digits)", type: "tel", required: true },
    { name: "organization.city", label: "City" },
  ];
  return (
    <main className="auth">
      <h1>Choose an organization</h1>
      {session.organizations.length === 0 && <p className="muted">You don’t belong to an organization yet. Create one to start managing properties.</p>}
      <ul className="rows">
        {session.organizations.map((o) => (
          <li key={o.organizationId}>
            <button className="pick" onClick={() => onPick(o.organizationId)}>
              <strong>{o.organizationName}</strong>
              <span className="muted">{[...new Set(o.roles.map((r) => r.role))].join(", ")}</span>
            </button>
          </li>
        ))}
      </ul>
      <button className="btn primary" onClick={() => setCreating(true)}>Create organization</button>
      <button className="link" onClick={onLogout}>Log out</button>
      {creating && (
        <FormModal title="Create organization" fields={fields} label="Create" onClose={() => setCreating(false)}
          submit={poster("organization/create")}
          onSuccess={(d) => onCreated({ organizationId: d.id, organizationName: d.name, roles: [{ role: "admin" }] })} />
      )}
    </main>
  );
}

/* ---------- Screens ---------- */
const greeting = () => { const h = new Date().getHours(); return h < 12 ? "Good morning" : h < 17 ? "Good afternoon" : "Good evening"; };
const daysFromToday = (d) => Math.round((new Date(d) - new Date(new Date().toDateString())) / 864e5);
const dueLabel = (d) => { const n = daysFromToday(d); return n < 0 ? `${-n} day${n === -1 ? "" : "s"} overdue` : n === 0 ? "Due today" : `Due in ${n} day${n === 1 ? "" : "s"}`; };

function Dashboard({ org, lk, go, userName }) {
  const [s, setS] = useState(null);
  useEffect(() => {
    const q = (p, x) => get(`organizations/${org}/${p}`, { size: 100, ...x });
    (async () => {
      const [p, t, i, pay, posts] = await Promise.all([q("properties"), q("tenancies", { status: "active" }), q("invoices"), q("payments"), q("posts", { size: 3 })]);
      const props = p.data || [], ten = t.data || [], inv = i.data || [];
      const roomLists = await Promise.all(props.map((x) => q(`properties/${x.id}/rooms`)));
      const occupancy = props.map((x, k) => ({
        id: x.id, name: x.name,
        beds: (roomLists[k].data || []).reduce((a, r) => a + (r.capacity || 0), 0),
        used: ten.filter((y) => y.propertyId === x.id).length,
      }));
      const open = inv.filter((x) => OPEN.includes(x.status));
      const billed = inv.filter((x) => x.status !== "cancelled").reduce((a, x) => a + x.totalAmount, 0);
      const collected = inv.reduce((a, x) => a + (x.amountPaid || 0), 0);
      setS({
        props: props.length, tenants: ten.length, occupancy,
        beds: occupancy.reduce((a, x) => a + x.beds, 0),
        open, overdue: open.filter((x) => daysFromToday(x.dueDate) < 0).length,
        due: open.reduce((a, x) => a + x.totalAmount - x.amountPaid, 0),
        rate: billed ? Math.round((collected / billed) * 100) : 0,
        attention: [...open].sort((a, b) => a.dueDate.localeCompare(b.dueDate)).slice(0, 5),
        payments: (pay.data || []).slice(0, 5), posts: posts.data || [],
      });
    })();
  }, [org]);

  const today = new Date().toLocaleDateString("en-IN", { weekday: "long", day: "numeric", month: "long" });
  const head = (
    <div className="greet">
      <div>
        <p className="muted">{today}</p>
        <h1>{greeting()}, {userName?.split(" ")[0]}</h1>
      </div>
      <div className="bar">
        <button className="btn" onClick={() => go("posts")}>New notice</button>
        <button className="btn" onClick={() => go("invoices")}>Record payment</button>
        <button className="btn primary" onClick={() => go("tenancies")}>Add tenancy</button>
      </div>
    </div>
  );
  if (!s) return <>{head}<p className="muted">Loading…</p></>;

  const pct = s.beds ? Math.round((s.tenants / s.beds) * 100) : 0;
  const Card = ({ title, link, to, children }) => (
    <section className="card">
      <div className="card-head"><h2>{title}</h2><button className="link" onClick={() => go(to)}>{link}</button></div>
      {children}
    </section>
  );
  return (
    <>
      {head}
      <dl className="stats">
        <div><dt>Properties</dt><dd>{s.props}</dd><span className="muted">{s.beds} beds in total</span></div>
        <div><dt>Occupancy</dt><dd>{pct}%</dd><span className="muted">{s.tenants} of {s.beds} beds filled</span></div>
        <div><dt>Unpaid invoices</dt><dd>{s.open.length}</dd><span className="muted">{s.overdue ? `${s.overdue} past due` : "None past due"}</span></div>
        <div><dt>Outstanding</dt><dd>{money(s.due)}</dd><span className="muted">{s.rate}% of billing collected</span></div>
      </dl>

      <div className="grid2">
        <Card title="Needs attention" link="All invoices" to="invoices">
          {s.attention.length === 0 ? <p className="muted">Every invoice is paid.</p> : (
            <ul className="list">
              {s.attention.map((x) => (
                <li key={x.id}>
                  <div>
                    <strong>{lk.users[x.userId] ?? `User #${x.userId}`}</strong>
                    <span className="muted">{x.invoiceType} · {x.invoiceNumber}</span>
                  </div>
                  <div className="right">
                    <strong>{money(x.totalAmount - x.amountPaid)}</strong>
                    <span className={daysFromToday(x.dueDate) < 0 ? "late" : "muted"}>{dueLabel(x.dueDate)}</span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Card>

        <Card title="Occupancy by property" link="Properties" to="properties">
          {s.occupancy.length === 0 ? <p className="muted">No properties yet.</p> : (
            <ul className="list">
              {s.occupancy.map((x) => {
                const p = x.beds ? Math.round((x.used / x.beds) * 100) : 0;
                return (
                  <li key={x.id} className="stack">
                    <div className="row">
                      <button className="link plain" onClick={() => go(`properties/${x.id}`)}>{x.name}</button>
                      <span className="muted">{x.used}/{x.beds} beds · {p}%</span>
                    </div>
                    <div className="meter" role="img" aria-label={`${p}% occupied`}><span style={{ width: `${p}%` }} /></div>
                  </li>
                );
              })}
            </ul>
          )}
        </Card>

        <Card title="Recent payments" link="All payments" to="payments">
          {s.payments.length === 0 ? <p className="muted">No payments recorded yet.</p> : (
            <ul className="list">
              {s.payments.map((x) => (
                <li key={x.id}>
                  <div><strong>{money(x.amount)}</strong><span className="muted">{x.method?.replaceAll("_", " ")}{x.reference ? ` · ${x.reference}` : ""}</span></div>
                  <span className="muted right">{day(x.paidAt)}</span>
                </li>
              ))}
            </ul>
          )}
        </Card>

        <Card title="Latest notices" link="All notices" to="posts">
          {s.posts.length === 0 ? <p className="muted">No notices posted.</p> : (
            <ul className="list">
              {s.posts.map((x) => (
                <li key={x.id} className="stack">
                  <strong>{x.title}</strong>
                  <span className="muted">{lk.props[x.propertyId] ?? "All properties"} · {day(x.publishAt)}</span>
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>
    </>
  );
}

/* ---------- Account ---------- */
function Account({ session, org, onSwitch, onLogout }) {
  const [saved, setSaved] = useState(false);
  const fields = [
    { name: "currentPassword", label: "Current password", type: "password", required: true },
    { name: "newPassword", label: "New password", type: "password", required: true },
    { name: "confirmPassword", label: "Confirm new password", type: "password", required: true },
  ];
  return (
    <Page title="Account">
      <p>
        <strong>{session.user.fullName}</strong>
        <br />
        <span className="muted">
          {session.user.email} · User ID {session.user.id} · {org.organizationName} ({[...new Set(org.roles.map((r) => r.role))].join(", ")})
        </span>
      </p>
      <div className="bar">
        <button className="btn" onClick={onSwitch}>Switch organization</button>
        <button className="btn" onClick={onLogout}>Log out</button>
      </div>
      <h2 className="sub">Change password</h2>
      <div className="narrow">
        <Form fields={fields} label="Change password" submit={poster("auth/change-password")} onSuccess={() => setSaved(true)} />
        {saved && <p className="ok" role="status">Password changed.</p>}
      </div>
    </Page>
  );
}

/* ---------- Shell ---------- */
// Routes live in the URL hash: #/properties/5 -> view "properties", arg "5".
function useRoute() {
  const read = () => window.location.hash.replace(/^#\/?/, "").split("/");
  const [r, setR] = useState(read);
  useEffect(() => {
    const f = () => setR(read());
    window.addEventListener("hashchange", f);
    return () => window.removeEventListener("hashchange", f);
  }, []);
  return [r[0] || "", r[1] || "", (p) => { window.location.hash = `/${p}`; }];
}

function Shell({ session, org, onSwitch, onLogout }) {
  const roles = org.roles.map((r) => r.role);
  const admin = roles.includes("admin");
  const staff = admin || roles.includes("manager");
  const id = org.organizationId;
  const [view, arg, go] = useRoute();
  const [lk, setLk] = useState({ props: {}, users: {} });

  useEffect(() => {
    const map = (r, name) => (r.success ? Object.fromEntries(r.data.map((x) => [x.id, x[name]])) : {});
    get(`organizations/${id}/properties`, { size: 100 }).then((r) => setLk((l) => ({ ...l, props: map(r, "name") })));
    if (staff) get(`organizations/${id}/users`, { size: 100 }).then((r) => setLk((l) => ({ ...l, users: map(r, "fullName") })));
  }, [id, staff]);

  const nav = staff
    ? [["dashboard", "Dashboard"], ["properties", "Properties"], ["tenancies", "Tenancies"], ["invoices", "Invoices"], ["payments", "Payments"],
       ["posts", "Posts"], ["forms", "Forms"], ["users", "Users"], ...(admin ? [["settings", "Settings"]] : []), ["account", "Account"]]
    : [["tenancies", "My tenancy"], ["invoices", "My invoices"], ["payments", "My payments"], ["posts", "Notices"], ["forms", "Forms"], ["account", "Account"]];
  const v = nav.some((n) => n[0] === view) ? view : nav[0][0];
  const p = { org: id, staff, admin, lk, arg: v === view ? arg : "", go, userName: session.user.fullName };
  const screens = {
    dashboard: Dashboard, properties: Properties, tenancies: Tenancies, invoices: Invoices, payments: Payments,
    posts: Posts, forms: Forms, users: Users, settings: Settings,
  };
  const Screen = screens[v];

  return (
    <div className="shell">
      <nav className="nav" aria-label="Main">
        <div className="brand">CommunityHub</div>
        <div className="org muted">{org.organizationName}</div>
        {nav.map(([k, l]) => (
          <button key={k} className={v === k ? "active" : ""} aria-current={v === k ? "page" : undefined} onClick={() => go(k)}>{l}</button>
        ))}
      </nav>
      <main className="main">
        {v === "account" ? <Account session={session} org={org} onSwitch={onSwitch} onLogout={onLogout} /> : <Screen {...p} />}
      </main>
    </div>
  );
}

export default function App() {
  const [s, setS] = useState(store.read);
  useEffect(() => store.write(s), [s]);
  useEffect(() => setAuthLostHandler(() => setS(null)), []);

  const logout = async () => { await api("auth/logout"); setS(null); };
  if (!s?.user) return <Auth onLogin={(d) => setS({ ...d, orgId: d.organizations.length === 1 ? d.organizations[0].organizationId : null })} />;

  const org = s.organizations.find((o) => o.organizationId === s.orgId);
  if (!org)
    return <OrgPicker session={s} onLogout={logout} onPick={(orgId) => setS({ ...s, orgId })}
      onCreated={(o) => setS({ ...s, organizations: [...s.organizations, o], orgId: o.organizationId })} />;
  return <Shell key={org.organizationId} session={s} org={org} onSwitch={() => setS({ ...s, orgId: null })} onLogout={logout} />;
}
