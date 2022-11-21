SUMMARY = "Eclipse IDE for Nuvoton"
LICENSE = "EPL-1.0"
LIC_FILES_CHKSUM = "file://uninstall.sh;md5=39df560014ac09d72b216a22f2a68ce1"

inherit native

SRC_URI = "https://www.nuvoton.com/opencms/resource-download.jsp?tp_GUID=SW1120220429172024;downloadfilename=nu-eclipse.tar.gz;name=nu-eclipse"
SRC_URI[nu-eclipse.md5sum] = "c1d3500fec22998f22f3f6f201736a25"
SRC_URI[nu-eclipse.sha256sum] = "fa13f04f408432511e530ae505450211606703e7bd59a2dfd71af71ac09abf9e"

S = "${WORKDIR}/NuEclipse_V1.02.019_Linux_Setup"

do_install() {
	install -d ${D}/${datadir}/nu-eclipse
	cp -r ${S}/. ${D}${datadir}/nu-eclipse
}

INSANE_SKIP_${PN} = "already-stripped file-rdeps sysroot_strip ldflag"
LLOW_EMPTY_${PN} = "1"

INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
