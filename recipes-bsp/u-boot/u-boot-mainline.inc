# Copyright (C) 2026, meta-u-boot-mainline contributors
# SPDX-License-Identifier: MIT

# Shared by u-boot-mainline and u-boot-tools-mainline so both are built from
# the same source revision. Upstream tag v${PV}; SRCREV is the commit the tag
# points to.
PV = "2027.01-rc1"
SRCREV = "dfcfc4c9f78d1743f490a497082a4ec8e058aae8"
SRC_URI = "git://git.u-boot-project.org/u-boot/u-boot.git;protocol=https;branch=main;tag=v${PV}"

SUMMARY += "(mainline)"
