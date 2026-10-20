# Security policy

<!--
Copyright (C) 2026, meta-u-boot-mainline contributors
SPDX-License-Identifier: CC-BY-4.0
-->

This layer carries no patches and no code of its own beyond two recipes and
two scripts. A vulnerability in what it builds is almost always a
vulnerability in U-Boot itself or in the openembedded-core includes the
recipes reuse, and is best reported there:

* **U-Boot**: follow the U-Boot project's process, documented at
  <https://docs.u-boot.org/en/latest/develop/security.html>.
* **openembedded-core and the Yocto Project**: see the Yocto Project's
  security policy at <https://www.yoctoproject.org/development/security/>,
  or mail security AT yoctoproject DOT org.

For a problem in the recipes, configuration or scripts of this layer itself,
contact the layer maintainers through the repository that hosts this layer.
Please do not open a public issue for a vulnerability that is not yet fixed.
