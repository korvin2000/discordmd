# Markdown Chatlog Schema (chatlog-md-v1)

This specification defines a **highly parseable, retrieval-friendly Markdown** format to represent messenger/Discord-style conversations as a knowledge base for OpenAI retrieval/file-search.

Design goals:
- **Deterministic parsing** (line-oriented, stable delimiters)
- **High readability** for humans
- **Good retrieval chunking** (one message = one logical chunk)
- Supports **replies** via stable message IDs
- Works well when split by **channel + date range**

---

## 1) File layout

### 1.1 Recommended file naming

- One channel per file, optionally one time slice per file.

Examples:
- `discord__channel-neosr__2025-12-01__2025-12-31.md`
- `telegram__chat-project-x__2026-01.md`

### 1.2 Top-of-file metadata (YAML front matter)

Use YAML front matter for **global metadata**. Keep it small and stable.

```yaml
---
schema: chatlog-md-v1
platform: discord            # discord | telegram | whatsapp | etc.
channel: "#neoSR"            # exact display name
channel_id: "1234567890"      # optional, if available
conversation_from: 2025-12-01
conversation_to: 2025-12-31
timezone: "Europe/Berlin"      # used for all timestamps unless overridden
exported_at: 2026-01-03T15:10:00+01:00
source: "discord-export vX"    # optional: exporter name/version
id_namespace: "discord"         # how to interpret message IDs
---
```

### 1.3 Human-visible header

Immediately after the YAML front matter, place a concise header for humans.

```md
# #neoSR

**Platform:** Discord  
**Date range:** 2025-12-01 — 2025-12-31 (Europe/Berlin)
```

---

## 2) Message record format

Each message is a **single Markdown section** starting with an H3 heading. Everything until the next H3 belongs to that message.

### 2.1 Message heading (machine-parseable)

Canonical heading grammar:

```text
### <msg_id> | ts=<ISO-8601> | author=<display> | author_id=<id?> | type=<message|reply> | reply_to=<msg_id?>
```

Rules:
- The first token after `###` is the **message ID** (no spaces). Example: `m:1133557799`.
- Remaining tokens are **pipe-separated key=value pairs**.
- Required keys: `ts`, `author`, `type`.
- Optional keys: `author_id`, `reply_to`, `thread`, `edited`, `tags`.
- `ts` must be **ISO-8601 with timezone offset** (or `Z`).
- If `type=reply`, then `reply_to` should be present.

Example:
```md
### m:1133557799 | ts=2025-12-07T21:14:33+01:00 | author=Alex | author_id=u:42 | type=reply | reply_to=m:1133550000
```

### 2.2 Message body

The message body is plain Markdown. Recommended structure:

1) **(Optional) Reply context** block
2) Message content (free text, lists, code blocks)
3) (Optional) Attachments / embeds / reactions (structured)

#### 2.2.1 Reply context block

Include a short reply context block to improve retrieval even when the original message is not retrieved.

```md
> **Replying to:** m:1133550000  
> **Quoted (optional):** “Short excerpt of the replied-to message…”
```

Notes:
- Keep the quoted excerpt short (one sentence). It is only a retrieval hint.
- The **canonical link** is still `reply_to=<id>` in the message heading.

#### 2.2.2 Attachments (optional, structured)

If you want deterministic parsing, use a small bullet list with key/value pairs.

```md
- attachment: name=error.log type=text/plain size=12KB sha256=<hash?> url=<redacted?>
- attachment: name=screenshot.png type=image/png size=340KB sha256=<hash?>
```

#### 2.2.3 Reactions (optional)

```md
- reactions: "+1"=3 "heart"=1
```

---

## 3) Full example

```md
---
schema: chatlog-md-v1
platform: discord
channel: "#neoSR"
conversation_from: 2025-12-01
conversation_to: 2025-12-31
timezone: "Europe/Berlin"
exported_at: 2026-01-03T15:10:00+01:00
id_namespace: "discord"
---

# #neoSR

**Platform:** Discord  
**Date range:** 2025-12-01 — 2025-12-31 (Europe/Berlin)

### m:1133550000 | ts=2025-12-07T21:12:10+01:00 | author=Marlen | author_id=u:99 | type=message
We still see occasional checkerboard artifacts when enabling `adv_loss`.

- attachment: name=val_grid.png type=image/png size=412KB sha256=…

### m:1133557799 | ts=2025-12-07T21:14:33+01:00 | author=Alex | author_id=u:42 | type=reply | reply_to=m:1133550000
> **Replying to:** m:1133550000  
> **Quoted (optional):** “checkerboard artifacts when enabling adv_loss”

This often correlates with:
- too-strong discriminator early on
- incorrect resize kernel in the degradation pipeline

Try: warm-up without GAN for 1–2k iters, then ramp `gan_weight` linearly.

```yaml
gan_weight_schedule:
  type: linear
  start: 0.0
  end: 0.01
  steps: 2000
```

### m:1133562000 | ts=2025-12-07T21:20:02+01:00 | author=Pietro | type=message
Can we pin the exact config that produced the artifacts? I can reproduce on my side.
```

---

## 4) Parsing contract (technical)

### 4.1 Informal grammar

```pseudo
Document := FrontMatter? ChannelHeader Messages+

FrontMatter := "---" NEWLINE YAML_LINES+ "---" NEWLINE
ChannelHeader := "# " ChannelName NEWLINE (MarkdownLines)*

Messages := Message+
Message := MessageHeading NEWLINE MessageBody

MessageHeading := "### " MsgId (" | " KVPair)+
KVPair := Key "=" Value
Key := [a-zA-Z_][a-zA-Z0-9_]*
Value := (no " | " sequence; may contain spaces if you quote it)

MessageBody := LinesUntilNext("### ")
MsgId := "m:" NonSpace+
```

### 4.2 Deterministic parsing algorithm

```pseudo
function parse_chatlog(md_text):
  front_matter = parse_yaml_front_matter_if_present(md_text)
  channel_header = parse_until_first_h3(md_text)

  messages = []
  for each h3_section in split_by_regex(md_text, /^### /m):
    heading_line = first_line(h3_section)
    body = rest_lines(h3_section)

    msg = {}
    tokens = split(heading_line after "### ", " | ")
    msg.id = tokens[0].trim()

    for t in tokens[1:]:
      k, v = split_first(t, "=")
      msg[k.trim()] = unquote(v.trim())

    msg.content_md = body
    messages.append(msg)

  return {front_matter, channel_header, messages}
```

### 4.3 Normalization rules (recommended)

```pseudo
# Ensure stable retrieval + stable parsing
normalize(message):
  - ts: ISO-8601 with offset
  - author: display name without surrounding @
  - message IDs: prefix "m:" (even if source IDs are numeric)
  - if type=reply and reply_to missing => set type=message OR fix reply_to
  - preserve original message text verbatim in body
  - keep quoted reply excerpt <= 200 chars
```

---

## 5) Export/generation guidance (so an LLM can implement it)

### 5.1 Required input model

```pseudo
ChannelExport {
  platform: string
  channel_name: string
  channel_id?: string
  timezone: string
  from_date: date
  to_date: date
  exported_at: datetime
  messages: Message[]
}

Message {
  id: string              # stable unique id (discord snowflake etc.)
  ts: datetime            # timestamp
  author: string
  author_id?: string
  type: "message" | "reply"
  reply_to?: string
  text: string
  attachments?: Attachment[]
}

Attachment {
  name: string
  mime: string
  size_bytes?: int
  sha256?: string
  url?: string            # optionally redacted
}
```

### 5.2 Rendering procedure

```pseudo
render(ChannelExport ch):
  out = []

  out += yaml_front_matter({
    schema: "chatlog-md-v1",
    platform: ch.platform,
    channel: ch.channel_name,
    channel_id: ch.channel_id,
    conversation_from: format_date(ch.from_date),
    conversation_to: format_date(ch.to_date),
    timezone: ch.timezone,
    exported_at: format_iso(ch.exported_at),
    id_namespace: ch.platform
  })

  out += "# " + ch.channel_name + "\n\n"
  out += "**Platform:** " + titlecase(ch.platform) + "  \n"
  out += "**Date range:** " + format_date(ch.from_date) + " — " + format_date(ch.to_date)
  out += " (" + ch.timezone + ")\n\n"

  for m in sort_by_ts(ch.messages):
    heading = "### m:" + normalize_id(m.id)
    heading += " | ts=" + format_iso(m.ts)
    heading += " | author=" + escape_pipes(m.author)
    if m.author_id: heading += " | author_id=u:" + normalize_id(m.author_id)
    heading += " | type=" + m.type
    if m.type == "reply": heading += " | reply_to=m:" + normalize_id(m.reply_to)

    out += heading + "\n"

    if m.type == "reply":
      out += "> **Replying to:** m:" + normalize_id(m.reply_to) + "  \n"
      # optionally include a short excerpt if you can access original text
      # out += "> **Quoted (optional):** “...excerpt...”\n\n"
      out += "\n"

    out += m.text.trim() + "\n\n"

    if m.attachments not empty:
      for a in m.attachments:
        out += "- attachment: name=" + a.name
        out += " type=" + a.mime
        if a.size_bytes: out += " size=" + human_bytes(a.size_bytes)
        if a.sha256: out += " sha256=" + a.sha256
        if a.url: out += " url=" + redact_if_needed(a.url)
        out += "\n"
      out += "\n"

  return join(out, "")
```

---

## 6) Why this works well for retrieval/file-search

- **H3 per message** yields clean, stable chunk boundaries.
- **Key=value tokens** in headings are easy to parse and are strong lexical anchors for keyword search.
- **Reply references** preserve conversational structure without needing deep nesting.
- Optional **reply excerpt** improves relevance when the original referenced message is not retrieved.

---

## 7) Optional extensions (keep them consistent)

If you need them later, add keys (do not invent new syntax):
- `thread=<id>`
- `edited=true` and/or `edited_ts=<iso>`
- `role=moderator|bot|user`
- `lang=de|en|ru`
- `tags=["code","image","decision"]` (JSON-like string; keep it on one line)

Formal EBNF (chatlog-md-v1)

Notation: ISO/IEC-style EBNF. Terminals in quotes. ? ... ? denotes a lexical constraint (non-context-free checks).
Normalization requirement: input/output MUST be normalized to UTF-8 and LF line endings (\n) before parsing/generation.
```ebnf
(*
  chatlog-md-v1 — EBNF
  All lines end with LF (eol).
*)

document
  = [ front_matter ]
    channel_section
    { message_section }
    [ trailing_ws ] ;

front_matter
  = fm_delim , eol
    { yaml_line }
    fm_delim , eol ;

fm_delim
  = "---" ;

yaml_line
  = yaml_text , eol ;
yaml_text
  = ? any Unicode scalar sequence not equal to "---" and not containing LF ? ;

channel_section
  = channel_title , eol
    { channel_line } ;

channel_title
  = "#", sp, channel_name ;
channel_name
  = line_text ;

channel_line
  = non_message_heading_line ;

message_section
  = message_heading , eol
    message_body ;

message_heading
  = "###", sp, msg_id, sp, "|", sp, ts_pair,
    sp, "|", sp, author_pair,
    [ sp, "|", sp, author_id_pair ],
    sp, "|", sp, type_pair,
    [ sp, "|", sp, reply_to_pair ],
    { sp, "|", sp, extra_pair } ;

ts_pair
  = "ts", "=", timestamp ;

author_pair
  = "author", "=", value ;

author_id_pair
  = "author_id", "=", id_value ;

type_pair
  = "type", "=", ( "message" | "reply" ) ;

reply_to_pair
  = "reply_to", "=", msg_id ;

extra_pair
  = key, "=", value ;

key
  = alpha, { alpha | digit | "_" } ;

value
  = quoted_value | bare_value ;

quoted_value
  = "\"", { qchar }, "\"" ;

qchar
  = escape | qsafe ;

qsafe
  = ? any Unicode scalar except "\"", "\"", and LF ? ;

escape
  = "\\", ( "\\" | "\"" | "n" | "t" | "r" | ( "u", hex, hex, hex, hex ) ) ;

bare_value
  = bare_char, { bare_char } ;

bare_char
  = ? any Unicode scalar except "|" and LF ? ;

msg_id
  = "m:", id_token ;

id_value
  = ( "u:" | "m:" | "c:" | "t:" ), id_token ;

id_token
  = id_char, { id_char } ;

id_char
  = alpha | digit | "-" | "_" | "." ;

message_body
  = { body_line } ;

body_line
  = non_message_heading_line ;

non_message_heading_line
  = line_text , eol
    (* Semantic restriction: the line MUST NOT begin with "### " *) ;

line_text
  = { non_lf } ;

non_lf
  = ? any Unicode scalar except LF ? ;

timestamp
  = date, "T", time, timezone ;

date
  = year, "-", month, "-", day ;

time
  = hour, ":", minute, ":", second, [ fraction ] ;

fraction
  = ".", digit, { digit } ;

timezone
  = "Z" | ( ( "+" | "-" ), hour, ":", minute ) ;

year   = digit, digit, digit, digit ;
month  = digit, digit ;
day    = digit, digit ;
hour   = digit, digit ;
minute = digit, digit ;
second = digit, digit ;

alpha = "A"…"Z" | "a"…"z" ;
digit = "0"…"9" ;
hex   = digit | "A"…"F" | "a"…"f" ;

sp = " " ;
eol = "\n" ;

trailing_ws
  = { " " | "\t" | "\n" } ;
```
Semantic constraints (MUST/SHOULD)
Global (file-level)

File MUST be UTF-8, LF endings.

YAML front matter SHOULD be present and SHOULD contain at least:

schema: chatlog-md-v1

platform

channel

conversation_from, conversation_to (YYYY-MM-DD)

timezone (IANA name, e.g. Europe/Berlin)

exported_at (ISO-8601)

Messages MUST appear in non-decreasing timestamp order.

Tie-breaker: stable sort by normalized message id (lexicographic).

Message heading (required keys and meaning)

Each message_heading MUST include:

ts=<ISO-8601> (with Z or offset, never local time without offset)

author=<value> (quoted recommended)

type=message|reply

If type=reply, then reply_to=<msg_id> MUST exist.

msg_id MUST be unique within the file.

Message body boundary safety (critical)

Parsing relies on ^### (beginning of line) to detect message boundaries. Therefore:

Any body line that would begin with "### " MUST be escaped by the generator (see rules below), otherwise it will be mis-parsed as a new message heading.

Generation rules for Codex (no open questions)
1) Canonical output choices (fix ambiguity)

Codex MUST use these canonical choices when generating:

Always include YAML front matter.

Always quote these values in headings (even if they contain no spaces):

author="..."

any extra_pair that may contain spaces (e.g., thread="...", tags="...")

Use canonical message ID prefixing:

If source message id is 12345, emit m:12345

If an author_id is present, normalize as author_id=u:<id>.

2) Required YAML fields (defaults)

If a field is missing from the source export, Codex MUST apply defaults:

timezone: if unknown → "UTC"

conversation_from / conversation_to: derive from min/max message timestamps (date portion) if not provided

exported_at: generation time in ISO-8601 with offset (or Z if UTC)

channel_id: omit if not known

source: optional; omit if not known

3) Message ordering

Sort by ts ascending.

If equal timestamps, sort by normalized msg_id ascending.

4) Reply handling (two-layer)

For each reply message:

Heading MUST have: type=reply | reply_to=m:<id>

Body SHOULD begin with a reply context block (improves retrieval even when original isn’t retrieved):
```md
> **Replying to:** m:<id>
> **Quoted (optional):** "…"
```

Rules for Quoted (optional):
Include only if the original message text is available at generation time.
Excerpt MUST be ≤ 200 characters after whitespace normalization.
Excerpt MUST be a single line and MUST escape " as \" if used inside quoted_value elsewhere (the blockquote line itself can use plain Markdown quotes).

5) Body escaping rule (prevents accidental headings)
Before emitting m.text into the body:
For each line L in the message text:
If L starts with "### " then replace with "\u200B### " (prepend U+200B ZERO WIDTH SPACE).
Rationale: preserves human readability, prevents the parser from seeing a new message heading.
This rule MUST also be applied to any generated attachment/reaction lines if they could start with "### " (rare but deterministic).

6) Content preservation
Body text MUST preserve original message text verbatim except for:
line-ending normalization
the "### " escape rule above
Do not “clean up” user punctuation, casing, or code blocks.

Codex implementation pseudo-spec (deterministic)
Output model (assumed):
```pseudo
ChannelExport {
  platform: string
  channel_name: string
  channel_id?: string
  timezone?: string
  from_date?: date
  to_date?: date
  exported_at?: datetime
  messages: Message[]
}

Message {
  id: string
  ts: datetime_with_tz
  author: string
  author_id?: string
  reply_to?: string
  text: string
  attachments?: Attachment[]
  reactions?: map<string,int>
}
```

Rendering algorithm (example)
```pseudo
function render_chatlog_md(ch: ChannelExport) -> string:
  tz = ch.timezone ?? "UTC"

  msgs = sort(ch.messages, by ts asc, then by normalize_msg_id asc)

  from_date = ch.from_date ?? date(min(msgs.ts))
  to_date   = ch.to_date   ?? date(max(msgs.ts))
  exported_at = ch.exported_at ?? now_iso8601_with_offset(tz)

  out = []
  out += yaml_front_matter({
    schema: "chatlog-md-v1",
    platform: ch.platform,
    channel: ch.channel_name,
    channel_id: ch.channel_id (omit if null),
    conversation_from: from_date,
    conversation_to: to_date,
    timezone: tz,
    exported_at: exported_at,
    id_namespace: ch.platform
  })

  out += "# " + ch.channel_name + "\n\n"
  out += "**Platform:** " + titlecase(ch.platform) + "  \n"
  out += "**Date range:** " + from_date + " — " + to_date + " (" + tz + ")\n\n"

  id_to_text_excerpt = map()  # optional if you want reply excerpts

  for m in msgs:
    mid = "m:" + normalize_id(m.id)
    heading = "### " + mid
    heading += " | ts=" + iso8601(m.ts)
    heading += " | author=" + quote(m.author)
    if m.author_id: heading += " | author_id=" + "u:" + normalize_id(m.author_id)

    if m.reply_to exists:
      heading += " | type=reply"
      heading += " | reply_to=" + "m:" + normalize_id(m.reply_to)
    else:
      heading += " | type=message"

    out += heading + "\n"

    if m.reply_to exists:
      out += "> **Replying to:** m:" + normalize_id(m.reply_to) + "  \n"
      if id_to_text_excerpt contains reply_to:
        out += "> **Quoted (optional):** \"" + id_to_text_excerpt[reply_to] + "\"\n"
      out += "\n"

    body = escape_body(m.text)   # apply U+200B for lines starting with "### "
    out += body.trim_end() + "\n\n"

    if m.attachments not empty:
      for a in m.attachments:
        out += "- attachment: name=" + a.name + " type=" + a.mime
        if a.size_bytes: out += " size=" + human_bytes(a.size_bytes)
        if a.sha256: out += " sha256=" + a.sha256
        if a.url: out += " url=" + redact_if_needed(a.url)
        out += "\n"
      out += "\n"

    if m.reactions not empty:
      out += "- reactions: " + format_reactions(m.reactions) + "\n\n"

    id_to_text_excerpt[mid] = make_excerpt(m.text, 200)  # optional
  return concat(out)

function escape_body(text):
  lines = split_by_LF(normalize_LF(text))
  for i in range(len(lines)):
    if starts_with(lines[i], "### "):
      lines[i] = "\u200B" + lines[i]
  return join(lines, "\n")
```

Quick validation checklist (Codex MUST satisfy)

 YAML front matter present, schema: chatlog-md-v1
 # <channel> heading present
 Each message starts with ### m:<id> | ts=... | author="..." | type=...
 Replies include reply_to=m:<id> and body reply-context block
 No unescaped body line begins with ###
 Messages sorted by ts asc