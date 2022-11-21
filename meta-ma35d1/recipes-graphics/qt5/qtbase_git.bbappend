FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI+="file://linuxfb_doubleubffer.patch"

PACKAGECONFIG:append = " examples directfb tslib linuxfb fontconfig"
