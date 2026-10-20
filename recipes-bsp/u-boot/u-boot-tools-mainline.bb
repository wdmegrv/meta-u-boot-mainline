# Copyright (C) 2026, meta-u-boot-mainline contributors
# SPDX-License-Identifier: MIT

require recipes-bsp/u-boot/u-boot-common.inc
require recipes-bsp/u-boot/u-boot-tools.inc
require u-boot-mainline.inc

PROVIDES += "${MLPREFIX}u-boot-tools"
PROVIDES:class-native += "u-boot-tools-native"

# Stay out of the way unless selected, as base.bbclass does for virtual/*
# providers. Without this, bitbake's fallback ordering for multiple providers
# (alphabetical by PN) would pick this recipe for u-boot-mkimage-native etc.
# as soon as the layer is enabled.
python () {
    pn = d.getVar('PN')
    if bb.data.inherits_class('native', d):
        item = 'u-boot-tools-native'
    else:
        item = d.getVar('MLPREFIX') + 'u-boot-tools'
    pref = d.getVar('PREFERRED_PROVIDER_' + item)
    if pref != pn:
        raise bb.parse.SkipRecipe('PREFERRED_PROVIDER_%s is %s, not %s' % (item, pref or 'unset', pn))
}
