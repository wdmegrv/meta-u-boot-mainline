# AGENTS.md

<!--
Copyright (C) 2026, meta-u-boot-mainline contributors
SPDX-License-Identifier: CC-BY-4.0
-->

Guidelines for AI agents (and humans) working on meta-u-boot-mainline.

This layer is maintained agents first: routine maintenance is meant to be
done by an agent following this file, with a human reviewing and verifying
the result. `README.md` is the user-facing description, this file is the
maintainer's.

## What this layer is

A minimal Yocto Project layer that builds U-Boot release candidates and
releases straight from the tags on upstream `main`. Two recipes, no patches,
and nothing changes for a build until it opts in with
`require conf/u-boot-mainline/mainline.inc`.

## Layout

| Path | Purpose |
|------|---------|
| `conf/layer.conf` | Layer registration. `LAYERSERIES_COMPAT` names the supported Yocto releases. |
| `conf/u-boot-mainline/mainline.inc` | The opt-in: the `PREFERRED_PROVIDER` lines that select the recipes below. |
| `recipes-bsp/u-boot/u-boot-mainline.inc` | The version pin (`PV`, `SRCREV`) and the upstream repository, shared by both recipes. The only file an uprev touches. |
| `recipes-bsp/u-boot/u-boot-mainline.bb` | The bootloader. Only `require`s oe-core's `u-boot-common.inc` and `u-boot.inc`. |
| `recipes-bsp/u-boot/u-boot-tools-mainline.bb` | The host and target tools (`mkimage`, ...). `require`s `u-boot-tools.inc` and carries the SkipRecipe guard. |
| `scripts/update-layer` | Moves the pin to a new upstream tag and commits. |
| `kas/` | Build configurations: `base.yml` plus one `release-*.yml` and one `machine-*.yml`; `u-boot-mainline.yml` is the opt-in. |
| `scripts/build-layer` | Build stage: builds `virtual/bootloader` with kas and writes the artifacts directory. |
| `scripts/test-qemu` | Test stage: boots the artifacts in QEMU and checks the version. |
| `.github/workflows/forward-pr.yml` | Pushes each GitHub pull request to Forgejo as `gh-pr-<number>`, where CI runs, and deletes it on close. |

## Supported releases and branches

There is one branch, `main`. It supports every maintained Yocto Project
release in `LAYERSERIES_COMPAT` plus oe-core `master`; there are no
per-release branches. A dropped release gets no backports: use an older
commit of `main`.

## Invariants

Keep these true.

- **No patches, no bbappends, no copies of oe-core code.** The recipes only
  `require` the oe-core includes; the shared include adds the version pin
  and the upstream repository, which the oe-core includes of the supported
  releases get wrong since U-Boot moved to `git.u-boot-project.org` and
  branch `main`. A build fix for a new U-Boot tag belongs upstream, in
  U-Boot or oe-core, not here. Do not move the pin back to an
  older tag to work around a breakage; leave it and report the failure.
- **Enabling the layer changes nothing.** Until `mainline.inc` is required,
  every item it can select must still resolve to the recipe it resolved to
  before. `u-boot-mainline` is skipped by base.bbclass because it provides
  `virtual/bootloader`. `u-boot-tools-mainline` needs its own anonymous
  Python guard: bitbake breaks ties between different recipe names
  alphabetically, and `u-boot-tools-mainline-native` sorts before
  `u-boot-tools-native`, so without the guard enabling the layer would
  silently swap the host `mkimage`.
- **`PV` is the upstream tag without the `v`**, e.g. `2026.10-rc3`, dash
  included. `SRC_URI` fetches `tag=v${PV}`, so any other spelling breaks the
  fetch. Package managers translate the `-` themselves.
- **`SRCREV` is the commit the tag points to**, not the annotated tag object
  (the `^{}` line of `git ls-remote --tags`). The fetcher refuses a mismatch.
- **The pin lives in `u-boot-mainline.inc` only**, so the tools always match
  the bootloader.
- **`mainline.inc` sets every provider for target, `-native` and
  `nativesdk-`.**
- **`DEPENDS` in `u-boot-mainline.bb` matches the newest `u-boot_*.bb` in
  oe-core master.** New tags tend to need new host tools; oe-core's release
  recipe is where that shows up first. This is the one kind of recipe change
  the layer takes.
- **Every file starts with the copyright and SPDX header of its
  neighbours**: MIT for recipes, config and scripts, CC-BY-4.0 for
  documentation.
- **Scripts are Python 3.7+, standard library only, non-interactive**, and
  exit non-zero on failure. Agents run them: keep the output short and the
  errors specific.

## Testing

A test run has two stages:

```sh
scripts/build-layer --release wrynose --machine qemuarm64 --work-dir "$KAS_WORK_DIR"
scripts/test-qemu "$KAS_WORK_DIR/artifacts/wrynose-qemuarm64"
```

`build-layer` leaves the bootloader and a `build.json` in
`<work-dir>/artifacts/<release>-<machine>`, and `test-qemu` reads only that
directory, plus the QEMU whose path `build.json` records. That path points
into the build tree; where it is gone, `test-qemu` falls back to the QEMU in
`PATH`.

### In CI

The scripts are the CI interface; the pipeline only calls them:

- **Build and test in one job on one runner**, so the test uses the QEMU
  from the build. The test takes seconds; a separate job gains nothing.
- **Keep the work directory between runs.** Its downloads and sstate are
  what make repeated builds fast.
- **Cover every `kas/` release and machine.**
- **Keep the artifacts directory** as the job's artifact, so a failure can
  be looked at and the test repeated locally.

## Routine tasks

### Uprev to a new release candidate or release

1. Run `scripts/update-layer` for the latest upstream tag, or
   `scripts/update-layer 2026.10-rc4` for a specific one. It needs a clean
   tree and network access, rewrites the pin and commits
   `u-boot-mainline: Uprev to <version>`. Pass `--no-commit` to look first.
2. If the uprev needs a fix, make it and `git commit --amend`. Do not stack
   fix-up commits on an uprev, and never edit the pin by hand.

A release sorts after its release candidates, so "latest" moves from
`2026.10-rc4` to `2026.10` and on to `2027.01-rc1` by itself. Skipped tags
are fine: the layer tracks the latest tag, not every tag.

### New Yocto Project release

Do this when oe-core `master` adds the codename to `LAYERSERIES_CORENAMES`,
not at release: `master` may then reject older codenames.

1. Add the codename to `LAYERSERIES_COMPAT_meta-u-boot-mainline` in
   `conf/layer.conf`.
2. Compare `DEPENDS` in `u-boot-mainline.bb` with oe-core's newest
   `u-boot_*.bb` and take over what is missing.

Once oe-core has branched for the release, add `kas/release-<codename>.yml`,
copied from the newest one with the oe-core and bitbake branches changed.
Until then `release-master.yml` covers it.

### Drop a Yocto Project release

Drop a release at upstream end of life, or earlier if mainline U-Boot stops
building on it and no fix will reach that oe-core branch. Remove it from
`LAYERSERIES_COMPAT` and delete its `kas/release-<codename>.yml`; say why in
the commit body.

### Anything else

Changes outside the tasks above are rare by design. Before adding
anything, check it against the invariants; if it needs a patch, a bbappend,
or a copy of oe-core logic, it does not belong in this layer.

## Commit conventions

- Subject: `<area>: <Imperative summary>`, where the area is the recipe
  name or the directory (`scripts`, `conf`). Layer-wide changes have no
  prefix. Examples from the history: `u-boot-mainline: Uprev to
  2026.10-rc3`, `scripts: Add update-layer`.
- One logical change per commit. The body explains why when the subject
  does not make it obvious.
- Uprev commits are made by `scripts/update-layer`; keep its message.
- An agent never adds a `Signed-off-by`: only the human who reviews the
  change can certify the DCO, and adds their own.

## Unattended runs

An agent that runs the uprev on a schedule should:

- stop quietly when `update-layer` reports the pin already at the latest
  tag;
- leave a new uprev commit for a human to review instead of pushing it;
- on failure, report the error, leave the pin where it is, and not retry
  with an older tag.
