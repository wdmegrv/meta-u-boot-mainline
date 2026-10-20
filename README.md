# meta-u-boot-mainline

<!--
Copyright (C) 2026, meta-u-boot-mainline contributors
SPDX-License-Identifier: CC-BY-4.0
-->

A Yocto Project layer providing recipes for the mainline U-Boot release
candidates and releases from
[git.u-boot-project.org](https://git.u-boot-project.org/u-boot/u-boot).

## Goals and non-goals of this layer

* We follow the release candidates (vYYYY.MM-rcN) and releases (vYYYY.MM) on
  the upstream `main` branch, to make it easy to test the next U-Boot release
  before it ships.

* We carry no patches against upstream U-Boot.

* We aim to support all maintained Yocto Project releases and
  openembedded-core master from a single `main` branch.

* We do not aim to replace the `u-boot` recipe from openembedded-core or a
  vendor U-Boot. Use this layer as a dependency of your own BSP or integration
  layer.

## Contents of this layer

* `u-boot-mainline`: This recipe follows the tags on the upstream `main`
  branch. As this includes release candidates it is only recommended for
  testing and active development.

* `u-boot-tools-mainline`: The U-Boot host tools (`mkimage`, `mkenvimage`,
  `mkeficapsule`, ...) built from the same source revision as
  `u-boot-mainline`, so FIT images, boot scripts and environment images are
  created with tools that match the bootloader. Available as target, `-native`
  and `nativesdk-` variants, like `u-boot-tools` in openembedded-core.

Both recipes take their version from `recipes-bsp/u-boot/u-boot-mainline.inc`,
so moving to a new release candidate is a change to that one file.

## Updating the layer

`scripts/update-layer` moves the recipes to the latest upstream release or
release candidate and commits the change. Pass a version (e.g. `2026.10-rc3`)
to move to that tag instead, or `--no-commit` to leave the change uncommitted.
It requires a clean working tree and network access to query the upstream tags.

## Contributing

This layer is maintained agents first. The routine work, moving to the next
release candidate and following new Yocto Project releases, is meant to be
done by an AI coding agent following [AGENTS.md](AGENTS.md), with a human
reviewing the result. AGENTS.md lists the invariants of the layer, the
routine tasks step by step and the conventions for commits; it is the
reference for human contributors as well.

## Using this layer

Add the layer to your `bblayers.conf`. Simply including the layer does not
change which U-Boot or U-Boot tools are built: `u-boot-mainline` is skipped
unless it is the preferred `virtual/bootloader`, and `u-boot-tools-mainline`
unless it is the preferred `u-boot-tools` (target), `u-boot-tools-native` or
`nativesdk-u-boot-tools`. To select the recipes from this layer, add the
following line to your `local.conf` or distro conf:

```bitbake
require conf/u-boot-mainline/mainline.inc
```

This sets `PREFERRED_PROVIDER_virtual/bootloader` and `PREFERRED_PROVIDER_u-boot`
to `u-boot-mainline`, and the `u-boot-tools`, `u-boot-mkimage`, `u-boot-mkenvimage`
and `u-boot-mkeficapsule` providers (target, `-native` and `nativesdk-`) to
`u-boot-tools-mainline`. Copy individual lines from that file if you only want
part of it, e.g. the mainline tools with your BSP's own U-Boot.

As with any U-Boot recipe, your machine configuration must set `UBOOT_MACHINE`
to the upstream defconfig name (e.g. `qemu_arm64_defconfig`).

## License

Recipes and config files are distributed under the MIT License. Documentation
files (such as this readme) are distributed under CC-BY-4.0.
