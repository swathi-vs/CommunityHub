import { useCallback, useEffect, useState } from "react";
import { api, get } from "./api";

export const money = (n) =>
  new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(n ?? 0);
export const day = (d) =>
  d ? new Date(d).toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" }) : "–";
export const opts = (a) => a.map((x) => [x, x.replaceAll("_", " ")]);
export const Badge = ({ s }) => <span className={`badge ${s}`}>{s?.replaceAll("_", " ")}</span>;

export const Page = ({ title, action, children }) => (
  <>
    <div className="page-head">
      <h1>{title}</h1>
      {action}
    </div>
    {children}
  </>
);

// Paged list: "next page if the last page was full", since the API returns no totals.
export function useList(path, params = {}) {
  const [rows, setRows] = useState(null);
  const [page, setPage] = useState(1);
  const [more, setMore] = useState(false);
  const [err, setErr] = useState("");
  const key = JSON.stringify(params);
  const load = useCallback(
    async (p, replace) => {
      const r = await get(path, { ...JSON.parse(key), page: p, size: 20 });
      if (!r.success) return setErr(r.message);
      setErr("");
      setMore(r.data.length === 20);
      setPage(p);
      setRows((old) => (replace || !old ? r.data : [...old, ...r.data]));
    },
    [path, key]
  );
  useEffect(() => {
    setRows(null);
    load(1, true);
  }, [load]);
  return { rows, err, more, next: () => load(page + 1), reload: () => load(1, true) };
}

export function List({ state, cols, empty, actions, children }) {
  const { rows, err, more, next } = state;
  if (err) return <p className="error" role="alert">{err}</p>;
  if (!rows) return <p className="muted">Loading…</p>;
  if (!rows.length) return <p className="muted">{empty}</p>;
  return (
    <>
      {children ? (
        rows.map(children)
      ) : (
        <div className="scroll">
          <table>
            <thead>
              <tr>
                {cols.map((c) => <th key={c[0]}>{c[0]}</th>)}
                {actions && <th><span className="sr-only">Actions</span></th>}
              </tr>
            </thead>
            <tbody>
              {rows.map((r) => (
                <tr key={r.id}>
                  {cols.map((c) => <td key={c[0]}>{c[1](r)}</td>)}
                  {actions && <td className="acts">{actions(r)}</td>}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {more && <button className="btn" onClick={next}>Load more</button>}
    </>
  );
}

// fields: { name (exact API param), label, type, options: [[value,label]], required, value, hint, step,
//           local (UI only, not sent), resets: [names of fields to clear when this one changes] }
const DT = /^\d{4}-\d\d-\d\dT\d\d:\d\d$/;
export const poster = (path, fields = []) => (v) => {
  const skip = fields.filter((f) => f.local).map((f) => f.name);
  const send = Object.fromEntries(
    Object.entries(v).filter(([k]) => !skip.includes(k)).map(([k, x]) => [k, DT.test(x) ? `${x}:00` : x])
  );
  return api(typeof path === "function" ? path(v) : path, send);
};

export function Form({ fields, submit, label, onSuccess, onChange }) {
  const [vals, setVals] = useState(() =>
    Object.fromEntries(fields.map((f) => [f.name, f.value ?? f.options?.[0]?.[0] ?? ""]))
  );
  const [errs, setErrs] = useState({});
  const [msg, setMsg] = useState("");
  const [busy, setBusy] = useState(false);
  const set = (n, val) => {
    const resets = fields.find((f) => f.name === n)?.resets || [];
    setVals((x) => ({ ...x, [n]: val, ...Object.fromEntries(resets.map((k) => [k, ""])) }));
    onChange?.(n, val);
  };

  const go = async (e) => {
    e.preventDefault();
    setBusy(true);
    setErrs({});
    setMsg("");
    const r = await submit(vals);
    setBusy(false);
    if (r.success) onSuccess?.(r.data, vals);
    else {
      setErrs(r.errors || {});
      setMsg(r.errors ? "Fix the fields marked below." : r.message);
    }
  };

  return (
    <form className="form" onSubmit={go}>
      {fields.map((f) => {
        const common = { id: f.name, required: f.required, onChange: (e) => set(f.name, e.target.value) };
        return (
          <div className={`field ${f.type === "checkbox" ? "check" : ""}`} key={f.name}>
            {f.type === "checkbox" ? (
              <label>
                <input type="checkbox" checked={vals[f.name] === "true"} onChange={(e) => set(f.name, String(e.target.checked))} /> {f.label}
              </label>
            ) : (
              <label htmlFor={f.name}>{f.label}</label>
            )}
            {f.options ? (
              <select {...common} value={vals[f.name]}>
                {f.options.map(([v, l]) => <option key={v} value={v}>{l}</option>)}
              </select>
            ) : f.type === "textarea" ? (
              <textarea {...common} rows={4} value={vals[f.name]} />
            ) : f.type === "checkbox" ? null : (
              <input {...common} type={f.type || "text"} step={f.step} value={vals[f.name]} aria-invalid={!!errs[f.name]} />
            )}
            {f.hint && <span className="muted">{f.hint}</span>}
            {errs[f.name] && <span className="error">{errs[f.name]}</span>}
          </div>
        );
      })}
      {msg && <p className="error" role="alert">{msg}</p>}
      <button className="btn primary" disabled={busy}>{busy ? "Saving…" : label}</button>
    </form>
  );
}

export function Modal({ title, onClose, children }) {
  useEffect(() => {
    const k = (e) => e.key === "Escape" && onClose();
    window.addEventListener("keydown", k);
    return () => window.removeEventListener("keydown", k);
  }, [onClose]);
  return (
    <div className="scrim" onClick={onClose}>
      <div className="modal" role="dialog" aria-modal="true" aria-label={title} onClick={(e) => e.stopPropagation()}>
        <h2>{title}</h2>
        {children}
        <button className="link" onClick={onClose}>Cancel</button>
      </div>
    </div>
  );
}

export function FormModal({ title, onClose, ...form }) {
  return (
    <Modal title={title} onClose={onClose}>
      <Form {...form} onSuccess={(d, v) => { form.onSuccess?.(d, v); onClose(); }} />
    </Modal>
  );
}
