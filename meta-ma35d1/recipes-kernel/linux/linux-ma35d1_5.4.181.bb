require linux-ma35d1.inc

# Copyright (C) 2019-2020
# Copyright 2019-2020 Nuvoton
# Released under the MIT license (see COPYING.MIT for the terms)
SRCBRANCH = "linux-5.4.y"
LOCALVERSION = "-${SRCBRANCH}"
KERNEL_SRCREV = "4274cd1a20489d057232c4575340ef2b739ed287"

KERNEL_SRC ?= "git://github.com/OpenNuvoton/MA35D1_linux-5.4.y.git;protocol=https;branch=master"
SRC_URI = "${KERNEL_SRC}"
SRC_REV = "${KERNEL_SRCREV}"
