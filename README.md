# Finmatica History Records — Time Machine plug-in for iDempiere

> **Status:** early community release · **Target:** iDempiere 13 "Orion" LTS
> **License:** [GPLv2](LICENSE.md) (same as iDempiere core)

`fin-history-records` is an OSGi plug-in suite that brings **record-level history
tracking** and **time-travel queries** to any iDempiere window. Once a table is
marked for history mode, every `INSERT/UPDATE/DELETE` is mirrored on a `_HST`
shadow table together with the validity interval (`HSTFromDate`, `HSTToDate`).
The Time Machine layer rewrites read queries transparently, so the same UI can
show the data **as it was at any past point in time**, with no Java code
change in the consumer.

A "Create Historical Record" toolbar button lets the operator snapshot the
state of a row at a given business date — useful for legal archiving,
versioned masters (price lists, BOMs, structures, taxes) and audit trails.


---

## Table of contents

1. [Features](#features)
2. [Architecture overview](#architecture-overview)
3. [Requirements](#requirements)
4. [Build](#build)
5. [Install](#install)
6. [Usage](#usage)
7. [Bundle layout](#bundle-layout)
8. [Compatibility matrix](#compatibility-matrix)
9. [Authors & contributors](#authors--contributors)
10. [License](#license)

---

## Features

| Feature | Status in this release | Notes |
|---|---|---|
| `HST_HistoryMode` flag on `AD_Table` (None / TimeMachine / Storicized / Versioned / Limited) | ✅ | dictionary 2pack |
| Automatic creation of `*_HST` shadow tables when `HST_HistoryMode` is set | ✅ | via model validator at table activation |
| Trigger-style mirroring of `INSERT/UPDATE/DELETE` to `*_HST` | ✅ | `HistoryRecordEventHandler` |
| Toolbar action **Create Historical Record** | ✅ | bundle `…ui.zk` |
| Transparent Time Machine query rewriting at JDBC layer | ✅ (requires upstream PR) | hook on `Convert.rewriteStatements()` |
| Lookup cache invalidation when history mode is active | ✅ (requires upstream PR) | hook on `MLookup` |
| Form-level read-only mode when reading historical data | ✅ (requires upstream PR) | hook on `GridField.isEditable()` |
| Time Machine date-picker UI panel | ⏸ not yet ported | available in original Finmatica edition |
| Verify Change Impact info window | ⏸ not yet ported | available in original Finmatica edition |
| Tree Maintenance UI | ⏸ not yet ported | available in original Finmatica edition |

---

## Architecture overview

The plug-in is split in **3 OSGi bundles** + **2 core extension points** that
must exist in `org.adempiere.base`:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          iDempiere core (13.x)                          │
│   ┌──────────────────────────────┐    ┌─────────────────────────────┐   │
│   │ ISQLStatementRewriter (PR-A) │    │   IUIBehaviour    (PR-B)    │   │
│   │  hook in Convert.java        │    │  hook in MLookup +          │   │
│   │  + DB_Oracle / DB_PostgreSQL │    │       GridField.isEditable  │   │
│   └──────────────────────────────┘    └─────────────────────────────┘   │
└─────────▲───────────────────────────────────────▲───────────────────────┘
          │                                       │
          │ OSGi DS                               │ OSGi DS
          │                                       │
┌─────────┴───────────────────────────────────────┴───────────────────────┐
│                  it.finmatica.history-records (core)                    │
│  • HSTValidator (model validator)                                       │
│  • HistoryRecordEventHandler (DB event handler)                         │
│  • Time Machine SQL rewriter (uses ISQLStatementRewriter)               │
│  • History UI behaviour (uses IUIBehaviour)                             │
│  • Bundle-Activator: structure migration of HST_* dictionary + DDL      │
└─────────────────────────────────────────────────────────────────────────┘
                              ▲
                              │ Require-Bundle
                              │
┌─────────────────────────────┴───────────────────────────────────────────┐
│              it.finmatica.history-records.ui.zk (UI)                    │
│  • HistoryRecordsAction (toolbar button)                                │
│  • CreateHistoryRecord (process)                                        │
│  • HSTProcessFactoryZk                                                  │
└─────────────────────────────────────────────────────────────────────────┘

  (both bundles aggregated by feature: it.finmatica.history-records-feature)
```

The two extension points (`ISQLStatementRewriter`, `IUIBehaviour`) are
**no-op by default**: when no service is registered, the core behaves
exactly as vanilla. This is what makes them suitable for upstream merge.

---

## Requirements

* **iDempiere 13 "Orion"** workspace (vanilla)
* **JDK 17**
* PostgreSQL 14+ *or* Oracle 19+
* Two upstream Pull Requests must be merged (or back-ported as a local
  patch on `release-13`):
  * [iDempiere PR #3279 — IDEMPIERE-7023 ISQLStatementRewriter](https://github.com/idempiere/idempiere/pull/3279)
  * [iDempiere PR #3280 — IDEMPIERE-7024 IUIBehaviour](https://github.com/idempiere/idempiere/pull/3280)

Without these PRs the plug-in **still installs and the toolbar button works**,
but the transparent Time Machine query rewriting and lookup cache invalidation
are disabled.

---

## Build

The current release follows the **clone-inside-workspace** model. Drop the 3
bundles into an existing iDempiere workspace and build with the standard
iDempiere Maven reactor:

```sh
# 1. clone next to org.adempiere.base, NOT inside another folder
cd <your-idempiere-workspace>/
git clone https://github.com/scolettads/fin-history-records.git
mv fin-history-records/it.finmatica.history-records          .
mv fin-history-records/it.finmatica.history-records.ui.zk    .
mv fin-history-records/it.finmatica.history-records-feature  .

# 2. register the 3 modules in the root pom.xml
#    (add <module>it.finmatica.history-records</module>          )
#    (add <module>it.finmatica.history-records.ui.zk</module>    )
#    (add <module>it.finmatica.history-records-feature</module>  )

# 3. add the feature to org.adempiere.server-feature/server.product
#    <feature id="it.finmatica.history-records-feature"/>

# 4. build
./mvnw verify -DskipTests
```

A standalone Maven build (`mvn package` directly inside this repo, without
the iDempiere reactor) is planned for the next release.

## Install

If you have a compiled iDempiere 13 server, the artefacts to drop into
`plugins/` are:

```
plugins/it.finmatica.history-records_13.0.0.<qualifier>.jar
plugins/it.finmatica.history-records.ui.zk_13.0.0.<qualifier>.jar
```

After restart, run the 2Pack at:

```
Application Dictionary → System Admin → 2Pack
```

importing the dictionary export shipped under `dictionary/` (work in
progress — will be added in a follow-up release together with a default
seed configuration).

## Usage

1. Open *Application Dictionary → Table & Column*, pick a table, and set
   `HST_HistoryMode = 'TimeMachine'` (or `Storicized`).
2. The plug-in creates the shadow `<tablename>_HST` automatically.
3. From any window backed by that table, click the **Create Historical Record**
   toolbar button to snapshot the current row at a chosen business date.
4. With both upstream PRs in place, any subsequent query against the table
   while `HistorySelectionData` is set returns the historicized rows for that
   date instead of the current ones.

---

## Bundle layout

```
fin-history-records/
├── README.md
├── LICENSE.md                                  (GPLv2 — same as iDempiere)
├── .gitignore
├── it.finmatica.history-records/               (core bundle)
│   ├── META-INF/MANIFEST.MF
│   ├── OSGI-INF/
│   │   ├── historyevent.xml                   (event handler factory)
│   │   ├── historysqlrewriter.xml             (ISQLStatementRewriter impl)
│   │   ├── historyuibehaviour.xml             (IUIBehaviour impl)
│   │   ├── historyvalidatorfactory.xml        (model validator factory)
│   │   ├── hstmodelfactory.xml                (model factory)
│   │   └── hstprocessfactory.xml              (process factory)
│   ├── lib/jsqlparser-idempiere.jar           (custom fork — see below)
│   ├── local-maven-repo/it/finmatica/…        (mirror of the jar above)
│   ├── pom.xml
│   ├── build.properties
│   └── src/it/finmatica/history/records/      (~70 .java files)
├── it.finmatica.history-records.ui.zk/         (UI bundle)
│   ├── META-INF/MANIFEST.MF
│   ├── OSGI-INF/                              (toolbar, process factory)
│   ├── pom.xml
│   ├── build.properties
│   └── src/…                                  (toolbar action + process)
└── it.finmatica.history-records-feature/       (aggregator feature)
    ├── feature.xml
    ├── pom.xml
    └── build.properties
```

### About `jsqlparser-idempiere.jar`

This is a **Finmatica-maintained fork of [JSqlParser]**, used to safely
rewrite SQL statements while preserving comments, hints and CTE syntax.
Upstream JSqlParser is dual-licensed Apache-2.0 / LGPL-2.1, both
GPLv2-compatible. The jar is committed under `local-maven-repo/` and
mirrored into `lib/` for Eclipse PDE classpath resolution; the Maven
build copies it from the local repo during `generate-sources`.

[JSqlParser]: https://github.com/JSQLParser/JSqlParser

---

## Compatibility matrix

| Plug-in version | iDempiere version | DB engines | PR-A (#3279) | PR-B (#3280) | Notes |
|---|---|---|---|---|---|
| `13.0.0` (this) | 13 Orion LTS  (at least)  | PG 14+, Oracle 19+ | required | required | first community release |




## Authors & contributors

* **Stefano Coletta** &lt;[s.coletta@ads.it](mailto:s.coletta@ads.it)&gt;
  — port to iDempiere 13, upstream PRs, community release
* **Finmatica S.p.A.** — original plug-in design and implementation
  ([www.finmatica.it](https://www.finmatica.it))
* **Associazione ERP Open Source Italia** — community sponsoring and review

Special thanks to iDempiere core team for reviewing
the upstream extension-point proposals (IDEMPIERE-7023, IDEMPIERE-7024).

---

## License

This program is free software; you can redistribute it and/or modify it
under the terms of the **GNU General Public License version 2** as
published by the Free Software Foundation. See [LICENSE.md](LICENSE.md)
for the full text. It is identical to the license of the iDempiere core.