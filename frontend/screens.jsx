import { useCallback, useEffect, useState } from "react";
import { api, get } from "./api";
import { Badge, Form, FormModal, List, Modal, Page, day, money, opts, poster, useList } from "./ui";

const INVOICE_TYPES = ["rent", "deposit", "mess", "utility", "penalty", "refund", "other"];
const INVOICE_STATUS = ["pending", "partial", "paid", "overdue", "cancelled"];
const METHODS = ["cash", "upi", "card", "bank_transfer", "cheque", "other"];
const VISIBILITY = ["org_wide", "property", "residents_only", "admins_only"];
const FIELD_TYPES = ["text", "textarea", "number", "decimal", "boolean", "date", "time", "enum"];
const base = (org) => `organizations/${org}`;
const ph = (t) => ["", t];
const yn = [["false", "No"], ["true", "Yes"]];
const slug = (s) => s.toLowerCase().replace(/[^a-z0-9]+/g, "_").replace(/^_|_$/g, "");

// Run a mutation, optionally after confirming; report failures plainly.
const act = async (path, params, done, ask) => {
  if (ask && !window.confirm(ask)) return;
  const r = await api(path, params);
  r.success ? done?.() : window.alert(r.message);
};

function useGet(path, params = {}) {
  const [d, setD] = useState(null);
  const [err, setErr] = useState("");
  const key = JSON.stringify(params);
  const load = useCallback(
    () => get(path, JSON.parse(key)).then((r) => (r.success ? (setErr(""), setD(r.data)) : setErr(r.message))),
    [path, key]
  );
  useEffect(() => { load(); }, [load]);
  return { d, err, reload: load };
}

const Bar = ({ children }) => <div className="bar">{children}</div>;
const Filter = ({ label, value, onChange, items }) => (
  <select aria-label={label} value={value} onChange={(e) => onChange(e.target.value)}>
    <option value="">{label}</option>
    {items.map(([v, l]) => <option key={v} value={v}>{l}</option>)}
  </select>
);
const Loading = ({ title, err }) => (
  <Page title={title}>{err ? <p className="error" role="alert">{err}</p> : <p className="muted">Loading…</p>}</Page>
);

/* ---------- Dynamic forms (one input per field definition) ---------- */
function DynamicForm({ org, formId, path, initial = {}, label = "Save", onSuccess }) {
  const f = useGet(`${base(org)}/forms/${formId}/fields`);
  if (f.err) return <p className="error" role="alert">{f.err}</p>;
  if (!f.d) return <p className="muted">Loading…</p>;
  if (!f.d.length) return <p className="muted">This form has no fields yet.</p>;
  const type = { text: "text", textarea: "textarea", number: "number", decimal: "number", boolean: "checkbox", date: "date", time: "time" };
  const fields = f.d.map((x) => ({
    name: x.fieldKey,
    label: x.fieldLabel + (x.isRequired ? "" : " (optional)"),
    hint: x.helpText,
    required: x.isRequired,
    type: type[x.fieldType],
    step: x.fieldType === "decimal" ? "any" : undefined,
    options: x.fieldType === "enum"
      ? [...(x.isRequired ? [] : [ph("—")]), ...(x.options || []).map((o) => [o.optionValue, o.optionLabel])]
      : undefined,
    value: initial[x.fieldKey] ?? x.defaultValue ?? (x.fieldType === "boolean" ? "false" : undefined),
  }));
  const submit = (v) =>
    api(path, { payload: JSON.stringify({ fields: fields.filter((g) => v[g.name] !== "").map((g) => ({ fieldKey: g.name, value: v[g.name] })) }) });
  return <Form fields={fields} submit={submit} label={label} onSuccess={onSuccess} />;
}

const kv = (rows) => (
  <dl className="kv">
    {rows.map((f) => <div key={f.fieldKey}><dt>{f.fieldLabel}</dt><dd>{f.value || "–"}</dd></div>)}
  </dl>
);

/* ---------- Rooms & beds helpers ---------- */
function useRoomBeds(org, pid0 = "") {
  const [pid, setPid] = useState(pid0);
  const [rid, setRid] = useState("");
  const [rooms, setRooms] = useState([]);
  const [beds, setBeds] = useState([]);
  useEffect(() => {
    setRooms([]);
    if (pid) get(`${base(org)}/properties/${pid}/rooms`, { size: 100 }).then((r) => r.success && setRooms(r.data));
  }, [org, pid]);
  useEffect(() => {
    setBeds([]);
    if (pid && rid) get(`${base(org)}/properties/${pid}/rooms/${rid}/beds`, { status: "available", size: 100 }).then((r) => r.success && setBeds(r.data));
  }, [org, pid, rid]);
  return {
    roomOpts: [ph("Select room"), ...rooms.map((r) => [r.id, `Room ${r.roomNumber}`])],
    bedOpts: [ph("Select an available bed"), ...beds.map((b) => [b.id, b.bedLabel])],
    onChange: (n, v) => {
      if (n === "propertyId") { setPid(v); setRid(""); }
      if (n === "tenancy.roomId") setRid(v);
    },
  };
}

function Beds({ org, pid, room, close }) {
  const beds = useList(`${base(org)}/properties/${pid}/rooms/${room.id}/beds`);
  const [m, setM] = useState(null);
  const edit = m && m !== "new";
  return (
    <section className="panel">
      <Bar>
        <h2>Beds in room {room.roomNumber}</h2>
        <button className="btn primary" onClick={() => setM("new")}>Add bed</button>
        <button className="link" onClick={close}>Close</button>
      </Bar>
      <List state={beds} empty="No beds yet. Add a bed to this room."
        cols={[["Bed", (b) => <strong>{b.bedLabel}</strong>], ["Status", (b) => <Badge s={b.status} />]]}
        actions={(b) => b.status !== "occupied" && <button className="link" onClick={() => setM(b)}>Edit</button>} />
      <p className="muted">Beds become occupied through tenancies. End the tenancy to free one.</p>
      {m === "new" && <FormModal title="Add bed" label="Add bed" onClose={() => setM(null)} onSuccess={beds.reload}
        fields={[{ name: "bed.bedLabel", label: "Bed label", required: true }]}
        submit={poster(`${base(org)}/properties/${pid}/rooms/${room.id}/beds/create`)} />}
      {edit && <FormModal title="Edit bed" label="Save changes" onClose={() => setM(null)} onSuccess={beds.reload}
        fields={[
          { name: "bed.bedLabel", label: "Bed label", required: true, value: m.bedLabel },
          { name: "bed.status", label: "Status", options: opts(["available", "maintenance"]), value: m.status },
        ]}
        submit={poster(`${base(org)}/beds/${m.id}/update`)} />}
    </section>
  );
}

/* ---------- Properties ---------- */
const addr = (p = {}) =>
  [["addressLine1", "Address"], ["city", "City"], ["state", "State"], ["pincode", "Pincode (6 digits)"]].map(([k, l]) => ({
    name: `property.${k}`, label: l, required: true, value: p[k],
  }));

function PropertyDetail({ org, id, admin, go }) {
  const prop = useGet(`${base(org)}/properties/${id}`);
  const rooms = useList(`${base(org)}/properties/${id}/rooms`);
  const forms = useGet(`${base(org)}/forms`, { size: 100, isActive: true });
  const [m, setM] = useState("");
  const [room, setRoom] = useState(null);
  if (!prop.d) return <Loading title="Property" err={prop.err} />;
  const { property: p, formId, fixedFields = [], customFields = [] } = prop.d;
  const P = `${base(org)}/properties/${id}`;
  const close = () => setM("");
  return (
    <Page title={p.name}
      action={<Bar>
        <button className="btn" onClick={() => setM("edit")}>Edit</button>
        {admin && <button className="btn" onClick={() => act(`${P}/delete`, {}, () => go("properties"), "Delete this property? This can’t be undone.")}>Delete</button>}
      </Bar>}>
      <button className="link" onClick={() => go("properties")}>All properties</button>
      <p className="muted">{p.addressLine1}, {p.city}, {p.state} {p.pincode} <Badge s={p.status} /></p>

      <h2 className="sub">Details</h2>
      {kv(fixedFields)}
      {formId ? kv(customFields) : <p className="muted">No details form is linked. Link one to record extra information about this property.</p>}
      <Bar>
        <button className="btn" onClick={() => setM("link")}>{formId ? "Change details form" : "Link a details form"}</button>
        {formId && <button className="btn" onClick={() => setM("details")}>Edit details</button>}
      </Bar>

      <h2 className="sub">Rooms</h2>
      <Bar><button className="btn primary" onClick={() => setM("room")}>Add room</button></Bar>
      <List state={rooms} empty="No rooms yet. Add a room, then add its beds."
        cols={[
          ["Room", (r) => <strong>{r.roomNumber}</strong>], ["Type", (r) => r.roomType || "–"], ["Capacity", (r) => r.capacity],
          ["Rent", (r) => (r.monthlyRent ? money(r.monthlyRent) : "–")], ["AC", (r) => (r.hasAc ? "Yes" : "No")], ["Status", (r) => <Badge s={r.status} />],
        ]}
        actions={(r) => (
          <>
            <button className="link" onClick={() => setRoom(r)}>Beds</button>
            <button className="link" onClick={() => act(`${P}/rooms/${r.id}/delete`, {}, () => { setRoom(null); rooms.reload(); }, `Delete room ${r.roomNumber}?`)}>Delete</button>
          </>
        )} />
      {room && <Beds org={org} pid={id} room={room} close={() => setRoom(null)} />}

      {m === "edit" && <FormModal title="Edit property" label="Save changes" onClose={close} onSuccess={prop.reload} submit={poster(`${P}/update`)}
        fields={[
          { name: "property.name", label: "Property name", required: true, value: p.name },
          ...addr(p),
          { name: "property.status", label: "Status", options: opts(["draft", "active", "paused", "closed"]), value: p.status },
        ]} />}
      {m === "room" && <FormModal title="Add room" label="Add room" onClose={close} onSuccess={rooms.reload} submit={poster(`${P}/rooms/create`)}
        fields={[
          { name: "room.roomNumber", label: "Room number", required: true },
          { name: "room.capacity", label: "Capacity (beds)", type: "number", required: true, value: "1" },
          { name: "room.roomType", label: "Room type (optional)" },
          { name: "room.monthlyRent", label: "Monthly rent (₹)", type: "number" },
          { name: "room.hasAc", label: "Air conditioning", options: yn },
        ]} />}
      {m === "link" && <FormModal title="Details form" label="Link form" onClose={close} onSuccess={prop.reload} submit={poster(`${P}/forms/associate`)}
        fields={[{
          name: "formRelationship.formId", label: "Form", required: true,
          hint: "Changing the form removes the details saved with the previous one.",
          options: [ph("Select a form"), ...(forms.d || []).map((f) => [f.id, f.formName])],
        }]} />}
      {m === "details" && (
        <Modal title="Property details" onClose={close}>
          <DynamicForm org={org} formId={formId} path={`${P}/details/submit`} label="Save details"
            initial={Object.fromEntries(customFields.map((f) => [f.fieldKey, f.value]))}
            onSuccess={() => { prop.reload(); close(); }} />
        </Modal>
      )}
    </Page>
  );
}

export function Properties({ org, admin, arg, go }) {
  const list = useList(`${base(org)}/properties`);
  const types = useGet("property-types");
  const [open, setOpen] = useState(false);
  if (arg) return <PropertyDetail org={org} id={arg} admin={admin} go={go} />;
  const tname = (id) => types.d?.find((t) => t.id === id)?.name ?? "–";
  return (
    <Page title="Properties" action={<button className="btn primary" onClick={() => setOpen(true)}>Add property</button>}>
      <List state={list} empty="No properties yet. Add your first property to start adding rooms."
        cols={[["Name", (p) => <strong>{p.name}</strong>], ["Type", (p) => tname(p.propertyTypeId)], ["City", (p) => p.city], ["Status", (p) => <Badge s={p.status} />]]}
        actions={(p) => <button className="link" onClick={() => go(`properties/${p.id}`)}>Open</button>} />
      {open && <FormModal title="Add property" label="Add property" onClose={() => setOpen(false)} onSuccess={list.reload}
        submit={poster(`${base(org)}/properties/create`)}
        fields={[
          { name: "property.name", label: "Property name", required: true },
          { name: "property.propertyTypeId", label: "Type", options: (types.d || []).map((t) => [t.id, t.name]) },
          ...addr(),
        ]} />}
    </Page>
  );
}

/* ---------- Tenancies ---------- */
function NewTenancy({ org, lk, onClose, onDone }) {
  const [tenants, setTenants] = useState([]);
  useEffect(() => { get(`${base(org)}/users`, { role: "tenant", size: 100 }).then((r) => r.success && setTenants(r.data)); }, [org]);
  const rb = useRoomBeds(org);
  const fields = [
    { name: "userId", label: "Tenant", local: true, required: true, hint: "Only users with the tenant role are listed. Assign the role in Users first.", options: [ph("Select tenant"), ...tenants.map((u) => [u.id, u.fullName])] },
    { name: "propertyId", label: "Property", local: true, required: true, resets: ["tenancy.roomId", "tenancy.bedId"], options: [ph("Select property"), ...Object.entries(lk.props)] },
    { name: "tenancy.roomId", label: "Room", required: true, resets: ["tenancy.bedId"], options: rb.roomOpts },
    { name: "tenancy.bedId", label: "Bed", required: true, options: rb.bedOpts },
    { name: "tenancy.startDate", label: "Start date", type: "date", required: true },
    { name: "tenancy.expectedEndDate", label: "Expected end date (optional)", type: "date" },
    { name: "tenancy.depositAmount", label: "Deposit (₹)", type: "number" },
    { name: "tenancy.depositStatus", label: "Deposit status", options: opts(["held", "refunded", "forfeited"]) },
  ];
  return <FormModal title="Add tenancy" label="Add tenancy" onClose={onClose} onSuccess={onDone} onChange={rb.onChange} fields={fields}
    submit={poster((v) => `${base(org)}/properties/${v.propertyId}/users/${v.userId}/tenancies/create`, fields)} />;
}

function MoveBed({ org, t, onClose, onDone }) {
  const rb = useRoomBeds(org, t.propertyId);
  return <FormModal title="Move to another bed" label="Move" onClose={onClose} onSuccess={onDone} onChange={rb.onChange}
    submit={poster(`${base(org)}/tenancies/${t.id}/update`)}
    fields={[
      { name: "tenancy.roomId", label: "Room", required: true, resets: ["tenancy.bedId"], options: rb.roomOpts },
      { name: "tenancy.bedId", label: "Bed", required: true, options: rb.bedOpts },
    ]} />;
}

export function Tenancies({ org, staff, lk }) {
  const [f, setF] = useState({ status: "", propertyId: "" });
  const list = useList(`${base(org)}/tenancies`, f);
  const [m, setM] = useState(null);
  const t = m?.t;
  const close = () => setM(null);
  return (
    <Page title={staff ? "Tenancies" : "My tenancy"}
      action={staff && (
        <Bar>
          <Filter label="Any status" value={f.status} onChange={(status) => setF({ ...f, status })} items={opts(["active", "vacated", "terminated"])} />
          <Filter label="All properties" value={f.propertyId} onChange={(propertyId) => setF({ ...f, propertyId })} items={Object.entries(lk.props)} />
          <button className="btn primary" onClick={() => setM({ k: "new" })}>Add tenancy</button>
        </Bar>
      )}>
      <List state={list} empty={staff ? "No tenancies match. Add one to place a tenant in a bed." : "You don’t have a tenancy yet. Ask your manager to add you to a room."}
        cols={[
          ...(staff ? [["Tenant", (x) => <strong>{lk.users[x.userId] ?? `User #${x.userId}`}</strong>]] : []),
          ["Property", (x) => lk.props[x.propertyId] ?? "–"], ["Started", (x) => day(x.startDate)], ["Ends", (x) => day(x.expectedEndDate)],
          ["Deposit", (x) => <>{money(x.depositAmount)} <span className="muted">{x.depositStatus}</span></>], ["Status", (x) => <Badge s={x.status} />],
        ]}
        actions={staff && ((x) => (
          <>
            <button className="link" onClick={() => setM({ k: "edit", t: x })}>Edit</button>
            {x.status === "active" && <>
              <button className="link" onClick={() => setM({ k: "move", t: x })}>Move bed</button>
              <button className="link" onClick={() => setM({ k: "invoice", t: x })}>Raise invoice</button>
              <button className="link" onClick={() => act(`${base(org)}/tenancies/${x.id}/update`, { "tenancy.status": "vacated" }, list.reload, "End this tenancy? The bed becomes available.")}>End</button>
            </>}
          </>
        ))} />
      {m?.k === "new" && <NewTenancy org={org} lk={lk} onClose={close} onDone={list.reload} />}
      {m?.k === "move" && <MoveBed org={org} t={t} onClose={close} onDone={list.reload} />}
      {m?.k === "edit" && <FormModal title="Edit tenancy" label="Save changes" onClose={close} onSuccess={list.reload}
        submit={poster(`${base(org)}/tenancies/${t.id}/update`)}
        fields={[
          { name: "tenancy.expectedEndDate", label: "Expected end date", type: "date", value: t.expectedEndDate },
          { name: "tenancy.depositAmount", label: "Deposit (₹)", type: "number", value: t.depositAmount },
          { name: "tenancy.depositStatus", label: "Deposit status", options: opts(["held", "refunded", "forfeited"]), value: t.depositStatus },
          { name: "tenancy.status", label: "Status", options: opts(["active", "vacated", "terminated"]), value: t.status },
        ]} />}
      {m?.k === "invoice" && <FormModal title="Raise invoice" label="Raise invoice" onClose={close}
        submit={poster(`${base(org)}/properties/${t.propertyId}/tenancies/${t.id}/invoices/create`)}
        fields={[
          { name: "invoice.invoiceType", label: "Type", options: opts(INVOICE_TYPES) },
          { name: "invoice.totalAmount", label: "Amount (₹)", type: "number", required: true },
          { name: "invoice.dueDate", label: "Due date", type: "date", required: true },
          { name: "invoice.description", label: "Note (optional)" },
        ]} />}
    </Page>
  );
}

/* ---------- Invoices & payments ---------- */
export function Invoices({ org, staff }) {
  const [status, setStatus] = useState("");
  const list = useList(`${base(org)}/invoices`, { status });
  const [m, setM] = useState(null);
  const i = m?.i;
  const left = (x) => x.totalAmount - x.amountPaid;
  return (
    <Page title={staff ? "Invoices" : "My invoices"}
      action={<Filter label="Any status" value={status} onChange={setStatus} items={opts(INVOICE_STATUS)} />}>
      <List state={list} empty="No invoices here yet."
        cols={[
          ["Invoice", (x) => <strong>{x.invoiceNumber}</strong>], ["Type", (x) => x.invoiceType], ["Due", (x) => day(x.dueDate)],
          ["Total", (x) => money(x.totalAmount)], ["Outstanding", (x) => money(left(x))], ["Status", (x) => <Badge s={x.status} />],
        ]}
        actions={staff && ((x) => (
          <>
            {!["paid", "cancelled"].includes(x.status) && <button className="link" onClick={() => setM({ k: "pay", i: x })}>Record payment</button>}
            <button className="link" onClick={() => setM({ k: "edit", i: x })}>Edit</button>
          </>
        ))} />
      {m?.k === "pay" && <FormModal title={`Record payment for ${i.invoiceNumber}`} label="Record payment" onClose={() => setM(null)} onSuccess={list.reload}
        submit={poster(`${base(org)}/invoices/${i.id}/payments/create`)}
        fields={[
          { name: "payment.amount", label: "Amount (₹)", type: "number", step: "any", required: true, value: String(left(i)), hint: `Outstanding: ${money(left(i))}` },
          { name: "payment.method", label: "Method", options: opts(METHODS) },
          { name: "payment.reference", label: "Reference (optional)" },
          { name: "payment.notes", label: "Notes (optional)" },
        ]} />}
      {m?.k === "edit" && <FormModal title={`Edit ${i.invoiceNumber}`} label="Save changes" onClose={() => setM(null)} onSuccess={list.reload}
        submit={(v) => api(`${base(org)}/invoices/update`, { ...v, "invoice.id": i.id })}
        fields={[
          { name: "invoice.description", label: "Note", value: i.description },
          { name: "invoice.invoiceType", label: "Type", options: opts(INVOICE_TYPES), value: i.invoiceType },
          { name: "invoice.totalAmount", label: "Amount (₹)", type: "number", step: "any", value: i.totalAmount },
          { name: "invoice.dueDate", label: "Due date", type: "date", value: i.dueDate },
          { name: "invoice.status", label: "Status", options: opts(INVOICE_STATUS), value: i.status, hint: "Partial and paid are set automatically when payments are recorded." },
        ]} />}
    </Page>
  );
}

export function Payments({ org, staff }) {
  const [method, setMethod] = useState("");
  const list = useList(`${base(org)}/payments`, { method });
  return (
    <Page title={staff ? "Payments" : "My payments"} action={<Filter label="Any method" value={method} onChange={setMethod} items={opts(METHODS)} />}>
      <List state={list} empty="No payments recorded yet."
        cols={[
          ["Paid on", (p) => day(p.paidAt)], ["Invoice", (p) => `#${p.invoiceId}`], ["Amount", (p) => <strong>{money(p.amount)}</strong>],
          ["Method", (p) => p.method?.replaceAll("_", " ")], ["Reference", (p) => p.reference || "–"], ["Notes", (p) => p.notes || "–"],
        ]} />
    </Page>
  );
}

/* ---------- Posts ---------- */
export function Posts({ org, staff, lk }) {
  const list = useList(`${base(org)}/posts`);
  const [m, setM] = useState(null);
  const pf = (p = {}) => [
    { name: "post.title", label: "Title", required: true, value: p.title },
    { name: "post.body", label: "Message", type: "textarea", required: true, value: p.body },
    { name: "post.visibility", label: "Who can see it", options: opts(VISIBILITY), value: p.visibility },
    { name: "post.publishAt", label: "Publish at (optional)", type: "datetime-local", value: p.publishAt?.slice(0, 16) },
    { name: "post.expiresAt", label: "Expires at (optional)", type: "datetime-local", value: p.expiresAt?.slice(0, 16) },
  ];
  const create = [{ name: "propertyId", label: "Property", local: true, required: true, options: [ph("Select property"), ...Object.entries(lk.props)] }, ...pf()];
  return (
    <Page title={staff ? "Posts" : "Notices"} action={staff && <button className="btn primary" onClick={() => setM({ k: "new" })}>New notice</button>}>
      <List state={list} empty="No notices right now.">
        {(p) => (
          <article className="notice" key={p.id}>
            <h2>{p.title}</h2>
            <p>{p.body}</p>
            <div className="muted">
              {lk.props[p.propertyId] ?? "All properties"} · {p.visibility?.replaceAll("_", " ")} · {day(p.publishAt)}
            </div>
            {staff && (
              <Bar>
                <button className="link" onClick={() => setM({ k: "edit", p })}>Edit</button>
                <button className="link" onClick={() => act(`${base(org)}/posts/${p.id}/delete`, {}, list.reload, "Delete this notice?")}>Delete</button>
              </Bar>
            )}
          </article>
        )}
      </List>
      {m?.k === "new" && <FormModal title="New notice" label="Publish" onClose={() => setM(null)} onSuccess={list.reload} fields={create}
        submit={poster((v) => `${base(org)}/properties/${v.propertyId}/posts/create`, create)} />}
      {m?.k === "edit" && <FormModal title="Edit notice" label="Save changes" onClose={() => setM(null)} onSuccess={list.reload} fields={pf(m.p)}
        submit={(v) => poster(`${base(org)}/posts/update`)({ ...v, "post.id": m.p.id })} />}
    </Page>
  );
}

/* ---------- Custom forms ---------- */
function RecordView({ org, formId, record, onClose }) {
  const r = useGet(`${base(org)}/forms/${formId}/records/${record.id}`);
  return (
    <Modal title={`Submission #${record.id}`} onClose={onClose}>
      {r.err ? <p className="error" role="alert">{r.err}</p> : r.d ? kv(r.d) : <p className="muted">Loading…</p>}
    </Modal>
  );
}

function FormDetail({ org, id, staff, go }) {
  const form = useGet(`${base(org)}/forms/${id}`);
  const fields = useGet(`${base(org)}/forms/${id}/fields`);
  const records = useList(`${base(org)}/forms/${id}/records`);
  const [m, setM] = useState(null);
  const F = `${base(org)}/forms/${id}`;
  const close = () => setM(null);
  const addField = [
    { name: "fieldDefinition.fieldKey", label: "Key", hint: "Lowercase letters, digits and _, starting with a letter.", required: true },
    { name: "fieldDefinition.fieldLabel", label: "Label", required: true },
    { name: "fieldDefinition.fieldType", label: "Type", options: opts(FIELD_TYPES) },
    { name: "fieldDefinition.isRequired", label: "Required", options: yn },
    { name: "fieldDefinition.helpText", label: "Help text (optional)" },
    { name: "fieldDefinition.defaultValue", label: "Default value (optional)" },
    { name: "fieldDefinition.displayOrder", label: "Display order", type: "number" },
    { name: "optionsText", label: "Choices (for the select type)", type: "textarea", local: true, hint: "One choice per line." },
  ];
  const submitField = (v) => {
    const { optionsText, ...rest } = v;
    const extra = {};
    if (rest["fieldDefinition.fieldType"] === "enum")
      optionsText.split("\n").map((s) => s.trim()).filter(Boolean).forEach((l, i) => {
        extra[`options[${i}].optionValue`] = slug(l);
        extra[`options[${i}].optionLabel`] = l;
        extra[`options[${i}].displayOrder`] = i + 1;
      });
    return api(`${F}/field-definitions/create`, { ...rest, ...extra });
  };
  return (
    <Page title={form.d?.formName ?? "Form"} action={!staff && <button className="btn primary" onClick={() => setM({ k: "fill" })}>Fill in</button>}>
      <button className="link" onClick={() => go("forms")}>All forms</button>
      {form.d?.description && <p className="muted">{form.d.description}</p>}
      {staff && (
        <>
          <h2 className="sub">Fields</h2>
          <Bar><button className="btn primary" onClick={() => setM({ k: "field" })}>Add field</button></Bar>
          <List state={{ rows: fields.d, err: fields.err }} empty="No fields yet. Add the first field."
            cols={[
              ["Label", (x) => <strong>{x.fieldLabel}</strong>], ["Key", (x) => x.fieldKey], ["Type", (x) => x.fieldType],
              ["Required", (x) => (x.isRequired ? "Yes" : "No")], ["Choices", (x) => x.options?.map((o) => o.optionLabel).join(", ") || "–"],
            ]}
            actions={(x) => (
              <>
                {x.fieldType === "enum" && <button className="link" onClick={() => setM({ k: "option", f: x })}>Add choice</button>}
                <button className="link" onClick={() => act(`${F}/field-definitions/${x.id}/delete`, {}, fields.reload, `Delete “${x.fieldLabel}”? Saved values for it are cleared.`)}>Delete</button>
              </>
            )} />
        </>
      )}
      <h2 className="sub">{staff ? "Submissions" : "My submissions"}</h2>
      <List state={records} empty="No submissions yet."
        cols={[["Submission", (r) => `#${r.id}`], ["Tenancy", (r) => `#${r.tenancyId}`], ["Submitted", (r) => day(r.createdAt)]]}
        actions={(r) => <button className="link" onClick={() => setM({ k: "view", r })}>View</button>} />
      {m?.k === "field" && <FormModal title="Add field" label="Add field" onClose={close} onSuccess={fields.reload} fields={addField} submit={submitField} />}
      {m?.k === "option" && <FormModal title={`Add choice to ${m.f.fieldLabel}`} label="Add choice" onClose={close} onSuccess={fields.reload}
        submit={poster(`${F}/field-definitions/${m.f.id}/options/create`)}
        fields={[{ name: "fieldOption.optionValue", label: "Value", required: true }, { name: "fieldOption.optionLabel", label: "Label", required: true }]} />}
      {m?.k === "fill" && (
        <Modal title={form.d?.formName ?? "Form"} onClose={close}>
          <DynamicForm org={org} formId={id} path={`${F}/records/submit`} label="Submit" onSuccess={() => { records.reload(); close(); }} />
        </Modal>
      )}
      {m?.k === "view" && <RecordView org={org} formId={id} record={m.r} onClose={close} />}
    </Page>
  );
}

export function Forms({ org, staff, arg, go }) {
  const list = useList(`${base(org)}/forms`, staff ? {} : { isActive: true });
  const [m, setM] = useState(null);
  if (arg) return <FormDetail org={org} id={arg} staff={staff} go={go} />;
  return (
    <Page title="Forms" action={staff && <button className="btn primary" onClick={() => setM({ k: "new" })}>New form</button>}>
      <List state={list} empty={staff ? "No forms yet. Create one for complaints, requests or property details." : "There are no forms to fill in right now."}
        cols={[
          ["Form", (f) => <><strong>{f.formName}</strong><div className="muted">{f.description}</div></>],
          ...(staff ? [["Status", (f) => <Badge s={f.isActive ? "active" : "inactive"} />]] : []),
        ]}
        actions={(f) => (
          <>
            {!staff && <button className="link" onClick={() => setM({ k: "fill", f })}>Fill in</button>}
            <button className="link" onClick={() => go(`forms/${f.id}`)}>{staff ? "Manage" : "My submissions"}</button>
            {staff && f.isActive && <button className="link" onClick={() => act(`${base(org)}/forms/${f.id}/delete`, {}, list.reload, `Deactivate “${f.formName}”?`)}>Deactivate</button>}
          </>
        )} />
      {m?.k === "new" && <FormModal title="New form" label="Create form" onClose={() => setM(null)} onSuccess={list.reload}
        submit={poster(`${base(org)}/form/create`)}
        fields={[{ name: "customForm.formName", label: "Form name", required: true }, { name: "customForm.description", label: "Description (optional)" }]} />}
      {m?.k === "fill" && (
        <Modal title={m.f.formName} onClose={() => setM(null)}>
          <DynamicForm org={org} formId={m.f.id} path={`${base(org)}/forms/${m.f.id}/records/submit`} label="Submit" onSuccess={() => setM(null)} />
        </Modal>
      )}
    </Page>
  );
}

/* ---------- Users & roles ---------- */
function Assign({ org, roles, lk, user, onClose, onDone }) {
  const fields = [
    ...(user ? [] : [{ name: "userId", label: "User ID", type: "number", local: true, required: true, hint: "Ask the person for the ID shown on their Account page." }]),
    { name: "userRole.roleId", label: "Role", options: roles.map((r) => [r.id, r.name]) },
    { name: "propertyId", label: "Limit to one property (optional)", local: true, options: [ph("All properties"), ...Object.entries(lk.props)] },
  ];
  const submit = (v) => poster(`${base(org)}${v.propertyId ? `/properties/${v.propertyId}` : ""}/users/${user ? user.id : v.userId}/roles/assign`, fields)(v);
  return <FormModal title={user ? `Assign role to ${user.fullName}` : "Add user by ID"} label="Assign role" onClose={onClose} onSuccess={onDone} fields={fields} submit={submit} />;
}

export function Users({ org, admin, lk }) {
  const roles = useGet("roles");
  const [role, setRole] = useState("");
  const list = useList(`${base(org)}/users`, { role });
  const [m, setM] = useState(null);
  const u = m?.u;
  return (
    <Page title="Users"
      action={<Bar>
        <Filter label="All roles" value={role} onChange={setRole} items={(roles.d || []).map((r) => [r.name, r.name])} />
        {admin && <button className="btn primary" onClick={() => setM({ k: "assign" })}>Add user by ID</button>}
      </Bar>}>
      <List state={list} empty="No users match this filter."
        cols={[["Name", (x) => <strong>{x.fullName}</strong>], ["ID", (x) => x.id], ["Email", (x) => x.email], ["Phone", (x) => x.phone], ["Status", (x) => <Badge s={x.status} />]]}
        actions={(x) => (
          <>
            <button className="link" onClick={() => setM({ k: "edit", u: x })}>Edit</button>
            {admin && <>
              <button className="link" onClick={() => setM({ k: "assign", u: x })}>Assign role</button>
              <button className="link" onClick={() => act(`${base(org)}/users/${x.id}/deactivate`, {}, list.reload, `Remove ${x.fullName} from this organization?`)}>Deactivate</button>
            </>}
          </>
        )} />
      {m?.k === "assign" && <Assign org={org} roles={roles.d || []} lk={lk} user={u} onClose={() => setM(null)} onDone={list.reload} />}
      {m?.k === "edit" && <FormModal title={`Edit ${u.fullName}`} label="Save changes" onClose={() => setM(null)} onSuccess={list.reload}
        submit={poster(`${base(org)}/users/${u.id}/update`)}
        fields={[
          { name: "user.fullName", label: "Full name", required: true, value: u.fullName },
          { name: "user.email", label: "Email", type: "email", required: true, value: u.email },
          { name: "user.phone", label: "Phone (10 digits)", type: "tel", value: u.phone },
          { name: "user.dob", label: "Date of birth", type: "date", value: u.dob },
          { name: "user.gender", label: "Gender", options: [ph("Not set"), ...opts(["male", "female", "other", "prefer_not_to_say"])], value: u.gender },
          { name: "user.nationality", label: "Nationality", value: u.nationality },
        ]} />}
    </Page>
  );
}

/* ---------- Organization settings (admin) ---------- */
export function Settings({ org }) {
  const o = useGet(`organizations/${org}`);
  const [saved, setSaved] = useState(false);
  if (!o.d) return <Loading title="Organization settings" err={o.err} />;
  const fields = [
    ["name", "Name"], ["businessEmail", "Business email", "email"], ["businessPhone", "Business phone (10 digits)", "tel"], ["gstNumber", "GST number"],
    ["addressLine1", "Address"], ["city", "City"], ["state", "State"], ["pincode", "Pincode"],
  ].map(([k, label, type]) => ({ name: `organization.${k}`, label, type, value: o.d[k] ?? "" }));
  return (
    <Page title="Organization settings">
      <div className="narrow">
        <Form fields={fields} label="Save changes" submit={poster(`organizations/${org}/update`)} onSuccess={() => setSaved(true)} />
        {saved && <p className="ok" role="status">Saved.</p>}
      </div>
    </Page>
  );
}
