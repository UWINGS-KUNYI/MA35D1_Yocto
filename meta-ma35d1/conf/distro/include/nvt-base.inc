# ma35d1 DISTRO for the FrameBuffer graphical backend.

DISTRO = "nvt-base"
DISTRO_NAME = "MA35D1 Unoffical Release Distro"
DISTRO_VERSION = "5.5-kirkstone"

DISTRO_CODENAME = "kirkstone"
SDK_VENDOR = "-pokysdk"
SDK_VERSION := "${DISTRO_VERSION}"

MAINTAINER = "KunYi <kunyi.chen@gmail.com>"

TARGET_VENDOR = "-poky"

DISTRO = "nvt-ma35d1"


PREFERRED_PROVIDER_udev = "systemd"
PREFERRED_PROVIDER_udev-utils = "systemd"


# =========================================================================
# DISTRO features
# =========================================================================
DISTRO_FEATURES ?= " vc8000 dcultrafb gc520 directfb virtualization systemd alsa"
VIRTUAL-RUNTIME_init_manager = "systemd"
DISTRO_FEATURES_BACKFILL_CONSIDERED += "sysvinit"

