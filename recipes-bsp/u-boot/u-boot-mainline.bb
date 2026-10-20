# Copyright (C) 2026, meta-u-boot-mainline contributors
# SPDX-License-Identifier: MIT

require recipes-bsp/u-boot/u-boot-common.inc
require recipes-bsp/u-boot/u-boot.inc
require u-boot-mainline.inc

PROVIDES += "u-boot"

DEPENDS += "bc-native dtc-native gnutls-native python3-pyelftools-native"
