# Copyright (C) 2026, meta-u-boot-mainline contributors
# SPDX-License-Identifier: MIT

# Shared by u-boot-mainline and u-boot-tools-mainline so both are built from
# the same source revision. Upstream tag v${PV}; SRCREV is the commit the tag
# points to.
PV = "2026.10"
SRCREV = "5508406582f6f7120dce0c4b3059819a41788db2"
SRC_URI = "git://git.u-boot-project.org/u-boot/u-boot.git;protocol=https;branch=main;tag=v${PV}"

SUMMARY += "(mainline)"
